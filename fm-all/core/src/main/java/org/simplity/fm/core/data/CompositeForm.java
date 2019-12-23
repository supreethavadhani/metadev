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

import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * represents a form that has child/subordinate forms
 *
 * @author simplity.org
 * @param <T>
 *            schema for the header form.
 *
 */
public class CompositeForm<T extends Schema> extends Form<T> {

	protected LinkedForm<?>[] linkedForms;

	/**
	 * called from the constructor of generated classes.
	 */
	protected void initialize() {
		for (final LinkedForm<?> lf : this.linkedForms) {
			lf.init(this.schema);
		}
	}

	/**
	 * parse input json into our standard data structure
	 *
	 * @param json
	 * @param forInsert
	 * @param ctx
	 * @return null in case of any validation error
	 */
	public CompositeData<T> parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx) {
		@SuppressWarnings("unchecked")
		final DataRow<T> dataRow = (DataRow<T>) this.schema.parseRow(json, forInsert, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return null;
		}

		final int nbrLinks = this.linkedForms.length;
		final DataTable<?>[] childData = new DataTable[nbrLinks];
		int i = -1;
		for (final LinkedForm<?> lf : this.linkedForms) {
			i++;
			final JsonArray arr = json.getAsJsonArray(lf.linkName);
			childData[i] = lf.parse(arr, forInsert, ctx);
		}

		if (!ctx.allOk()) {
			logger.error("Error while reading data for child-forms");
			return null;
		}
		return new CompositeData<>(dataRow, childData);
	}

	/**
	 * method that can be used to serve a service-request to get data
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void fetch(final IServiceContext ctx, final JsonObject payload) throws Exception {
		@SuppressWarnings("unchecked")
		final DataRow<T> dataRow = (DataRow<T>) this.schema.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final int nbrLinks = this.linkedForms.length;
		final DataTable<?>[] childData = new DataTable[nbrLinks];
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			final boolean ok = dataRow.fetch(handle);
			if (ok) {
				int idx = -1;
				for (final LinkedForm<?> lf : this.linkedForms) {
					idx++;
					childData[idx] = lf.fetch(handle, dataRow.rawData);
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

			this.schema.serializeToJson(dataRow.rawData, jw);
			int idx = -1;
			for (final LinkedForm<?> lf : this.linkedForms) {
				idx++;
				jw.name(lf.linkName);
				final DataTable<?> dt = childData[idx];
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
		final CompositeData<T> data = this.parse(payload, false, ctx);
		if (data == null) {
			return;
		}
		final DataRow<T> dataRow = data.getDataRow();
		final DataTable<?>[] childData = data.getChildData();
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			if (dataRow.update(handle) == false) {
				return false;
			}

			int idx = -1;
			for (final LinkedForm<?> lf : this.linkedForms) {
				idx++;
				if (lf.save(handle, childData[idx], dataRow.rawData) == false) {
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
		final CompositeData<T> data = this.parse(payload, true, ctx);
		if (data == null) {
			return;
		}
		final DataRow<T> dataRow = data.getDataRow();
		final DataTable<?>[] childData = data.getChildData();
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			if (dataRow.insert(handle) == false) {
				return false;
			}

			int idx = -1;
			for (final LinkedForm<?> lf : this.linkedForms) {
				idx++;
				if (lf.insert(handle, childData[idx], dataRow.rawData) == false) {
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
		@SuppressWarnings("unchecked")
		final DataRow<T> dataRow = (DataRow<T>) this.schema.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}

		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			boolean ok = dataRow.deleteFromDb(handle);

			if (!ok) {
				return false;
			}

			for (final LinkedForm<?> lf : this.linkedForms) {
				ok = lf.delete(handle, dataRow.rawData);
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
	@Override
	public IService getService(final IoType opern) {
		if (this.operations == null || this.operations[opern.ordinal()] == false) {
			return null;
		}

		switch (opern) {
		case Get:
			return new FormService(IoType.Get) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					CompositeForm.this.fetch(ctx, inputPayload);

				}
			};

		case Filter:
			return new FormService(IoType.Filter) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					CompositeForm.this.schema.filter(ctx, inputPayload);

				}
			};

		case Create:
			return new FormService(IoType.Create) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					CompositeForm.this.insert(ctx, inputPayload);

				}
			};

		case Update:
			return new FormService(IoType.Update) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					CompositeForm.this.update(ctx, inputPayload);

				}
			};

		case Bulk:
			logger.info("Bulk operation not allowed on composite form");
			return null;

		case Delete:
			return new FormService(IoType.Delete) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					CompositeForm.this.delete(ctx, inputPayload);

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
			return this.opern.name() + '_' + CompositeForm.this.name;
		}
	}

}
