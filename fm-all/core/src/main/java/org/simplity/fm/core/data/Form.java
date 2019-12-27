/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.data;

import java.io.IOException;

import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * a wrapper on schema to offer this as client-facing service
 *
 * @author simplity.org
 *
 */
public abstract class Form {
	static protected final Logger logger = LoggerFactory.getLogger(Form.class);

	protected final String name;
	protected final Schema schema;
	protected final Field[] localFields;
	protected final LinkedForm[] linkedForms;
	/*
	 * operations that are to be exposed thru this form. array corresponds to
	 * the ordinals of IoType
	 */
	protected final boolean[] operations;

	/**
	 * @return primary schema that this form is based on. null if this form is
	 *         NOT based on any schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 * @return unique name of this form across all forms
	 */
	public String getNamea() {
		return this.name;
	}

	/**
	 * parse input data into the form-date instance associated with the concrete
	 * class
	 *
	 * @param json
	 * @param forInsert
	 *            true if the operation is insert. This is used to validate
	 *            primary key input
	 * @param ctx
	 * @return FormData instance of the right concrete class if all ok. null in
	 *         case of any error in which case the ctx has the message(s)
	 */
	public FormData parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx) {
		final Object[] fieldValues = this.parseLocalFields(json, forInsert, ctx);
		DataObject dataObject = null;
		if (this.schema != null) {
			dataObject = this.schema.parseRow(json, forInsert, ctx, null, 0);
		}
		DataTable[] childData = null;
		if (this.linkedForms != null) {
			childData = new DataTable[this.linkedForms.length];
			for (int i = 0; i < this.linkedForms.length; i++) {
				final LinkedForm lf = this.linkedForms[i];
				childData[i] = lf.parse(json.getAsJsonArray(lf.linkName), forInsert, ctx);
			}
		}
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return null;
		}

		return this.newFormData(dataObject, fieldValues, childData);
	}

	/**
	 * concrete class puts the right code in the constructor to initialize these
	 * fields
	 */
	protected Form() {
		this.name = null;
		this.schema = null;
		this.localFields = null;
		this.linkedForms = null;
		this.operations = null;
	}

	/**
	 * called internally by concrete class to initialize the linked forms
	 */
	protected void initialize() {
		if (this.linkedForms != null) {
			for (final LinkedForm lf : this.linkedForms) {
				lf.init(this.schema);
			}
		}
	}

	/**
	 * delegated to the concrete class to instantiate the right concrete class
	 * of FormData
	 */
	protected abstract FormData newFormData(DataObject dataObject, Object[] fieldValues, DataTable[] childData);

	/**
	 * method that can be used to serve a service-request to get data
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void read(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataObject dataObject = this.schema.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final int nbrLinks = this.linkedForms.length;
		final DataTable[] childData = new DataTable[nbrLinks];
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			final boolean ok = dataObject.read(handle);
			if (ok) {
				int idx = -1;
				for (final LinkedForm lf : this.linkedForms) {
					idx++;
					childData[idx] = lf.fetch(handle, dataObject.fieldValues);
				}
				result[0] = true;
			}
			return true;
		}, true);

		if (!result[0]) {
			logger.error("No data found for the requested keys");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
			return;
		}
		try (JsonWriter jw = new JsonWriter(ctx.getResponseWriter())) {
			jw.beginObject();

			this.schema.serializeToJson(dataObject.fieldValues, jw);
			int idx = -1;
			for (final LinkedForm lf : this.linkedForms) {
				idx++;
				jw.name(lf.linkName);
				final DataTable dt = childData[idx];
				if (dt != null) {
					dt.serializeAsJson(jw);
				} else {
					jw.beginArray();
					jw.endArray();
				}
			}
			jw.endObject();

		} catch (final IOException e) {
			final String msg = "I/O error while serializing e=" + e + ". message=" + e.getMessage();
			logger.error(msg);
			ctx.addMessage(Message.newError(msg));
		}
	}

	/**
	 * update/save the data coming in form a client to the database
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void update(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final FormData data = this.parse(payload, false, ctx);
		if (data == null) {
			return;
		}
		final DataObject dataRow = data.getDataObject();
		final FormDataTable[] linkedData = data.getLinkedData();
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			if (dataRow.update(handle) == false) {
				return false;
			}

			int idx = -1;
			for (final LinkedForm lf : this.linkedForms) {
				idx++;
				if (lf.save(handle, linkedData[idx].getDataTable(), dataRow.fieldValues) == false) {
					return false;
				}
			}
			result[0] = true;
			return true;
		}, false);

		if (!result[0]) {
			logger.error("ALl validations succeded, but Update failed");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}
	}

	/**
	 * insert the data that has come from a client into the database
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void insert(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final FormData data = this.parse(payload, true, ctx);
		if (data == null) {
			return;
		}
		final DataObject dataObject = data.getDataObject();
		final FormDataTable[] linkedData = data.getLinkedData();
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			if (dataObject.insert(handle) == false) {
				return false;
			}

			int idx = -1;
			for (final LinkedForm lf : this.linkedForms) {
				idx++;
				if (lf.insert(handle, linkedData[idx].getDataTable(), dataObject.fieldValues) == false) {
					return false;
				}
			}
			result[0] = true;
			return true;
		}, false);

		if (!result[0]) {
			logger.error("ALl validations succeded, but insert failed");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}
	}

	/**
	 * delete rows from the database based on the data sent from a client
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void delete(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataObject dataRow = this.schema.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}

		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			boolean ok = dataRow.delete(handle);

			if (!ok) {
				return false;
			}

			for (final LinkedForm lf : this.linkedForms) {
				ok = lf.delete(handle, dataRow.fieldValues);
				if (!ok) {
					return false;
				}
			}
			result[0] = true;

			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not deleted, possibly because of time-stamp issues");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * get the service instance for the desired operation on this form
	 *
	 * @param opern
	 * @return service, or null if this form is not designed for this operation
	 */
	public IService getService(final IoType opern) {
		if (this.operations == null || this.operations[opern.ordinal()] == false) {
			return null;
		}

		/*
		 * simple service works on its schema.
		 */
		if (this.linkedForms == null) {
			return this.schema.getService(opern);
		}

		/*
		 * composite form. We have to manage linked form as well.
		 */
		switch (opern) {
		case Get:
			return new FormService(IoType.Get) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.read(ctx, inputPayload);

				}
			};

		case Filter:
			return new FormService(IoType.Filter) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.schema.filter(ctx, inputPayload);

				}
			};

		case Create:
			return new FormService(IoType.Create) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.insert(ctx, inputPayload);

				}
			};

		case Update:
			return new FormService(IoType.Update) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.update(ctx, inputPayload);

				}
			};

		case Bulk:
			logger.info("Bulk operation not allowed on composite form");
			return null;

		case Delete:
			return new FormService(IoType.Delete) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.delete(ctx, inputPayload);

				}
			};

		default:
			logger.error("Form operation {} not yet implemented", opern);
			return null;
		}
	}

	protected abstract class FormService implements IService {
		protected final IoType opern;

		protected FormService(final IoType opern) {
			this.opern = opern;
		}

		@Override
		public String getId() {
			return this.opern.name() + '_' + Form.this.name;
		}
	}

	private Object[] parseLocalFields(final JsonObject json, final boolean forInsert, final IServiceContext ctx) {
		if (this.localFields == null) {
			return null;
		}
		final Object[] values = new Object[this.localFields.length];
		for (int i = 0; i < this.localFields.length; i++) {
			final Field f = this.localFields[i];
			final String value = JsonUtil.getStringMember(json, f.name);
			f.parseIntoRow(value, values, forInsert, ctx, null, 0);
		}
		return values;
	}

}
