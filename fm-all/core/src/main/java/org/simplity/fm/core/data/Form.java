/*
 * Copyright (c) 2020 simplity.org
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

import java.sql.SQLException;
import java.util.List;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form is a client-side component. If a form is based on a record for its data,
 * then this class is generated to deliver services for that client-side
 * component.
 *
 * @author simplity.org
 * @param <T>
 *            record that describes the data behind this form
 *
 */
public abstract class Form<T extends Record> {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);
	/*
	 * name of this form. unique within an app
	 */
	private final String name;
	/*
	 * record that this form is based on
	 */
	protected final T record;

	/*
	 * what operations are allowed on this form
	 */
	private final boolean[] operations;

	/*
	 * linked forms
	 */
	protected final LinkedForm<?>[] linkedForms;
	private final boolean isDb;

	protected Form(final String name, final T record, final boolean[] operations, final LinkedForm<?>[] linkedForms) {
		this.name = name;
		this.record = record;
		this.operations = operations;
		this.isDb = record instanceof DbRecord;
		this.linkedForms = linkedForms;
	}

	/**
	 * @return true if this form is based on a db record. false otherwise
	 */
	public boolean isDb() {
		return this.isDb;
	}

	/**
	 *
	 * @return true if this form has linked forms. false otherwise.
	 */
	public boolean hasLinks() {
		return this.linkedForms != null;
	}

	/**
	 * read rows for the linked-forms
	 *
	 * @param rawData
	 *            for the record for this form
	 * @param writer
	 *            to which the read rows are to be serialized into
	 * @param handle
	 * @throws SQLException
	 */
	public void readLinkedForms(final Object[] rawData, final ISerializer writer, final DbHandle handle)
			throws SQLException {
		if (this.linkedForms != null) {
			for (final LinkedForm<?> link : Form.this.linkedForms) {
				link.read((DbRecord) this.record, writer, handle);
			}
		}
	}

	/**
	 * load keys from the input. input is suspect.
	 *
	 * @param inputObject
	 *            non-null
	 * @param ctx
	 *            non-null. any validation error is added to it
	 * @return true record with parsed values. null if any input fails
	 *         validation.
	 */
	public boolean parseKeys(final IInputObject inputObject, final IServiceContext ctx) {
		if (!this.isDb) {
			logger.error("This form is based on {} that is not a DbRecord. Keys can not be parsed");
			return false;
		}
		return ((DbRecord) this.record).parseKeys(inputObject, ctx);
	}

	/**
	 *
	 * @param operation
	 * @return a service for this operation on the form. null if the operation
	 *         is not allowed.
	 */
	public IService getService(final IoType operation) {
		if (!this.operations[operation.ordinal()]) {
			logger.info("{} operation is not allowed on record {}", operation, this.name);
			return null;
		}

		String serviceName = operation.name();
		serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + '_' + this.name;

		/*
		 * forms with links require form-based service
		 */
		if (this.linkedForms != null) {
			switch (operation) {
			case Get:
				return new Reader(serviceName);
			case Create:
				return new Creater(serviceName);
			case Update:
				return new Updater(serviceName);
			case Delete:
				return new Deleter(serviceName);
			case Filter:
				return new Filter(serviceName);
			default:
				throw new ApplicationError("Form needs to be designed for operation " + operation.name());
			}
		}

		/*
		 * form s simply a wrapper on the record..
		 */
		if (this.isDb) {
			return ((DbRecord) this.record).getService(operation, serviceName);
		}

		/*
		 * there is very little we can do as an auto-service. Just for
		 * testing/development purpose??
		 * we will add other features on
		 */

		final String sn = serviceName;
		final boolean forInsert = operation == IoType.Create;
		final T rec = this.record;
		return new IService() {

			@Override
			public void serve(final IServiceContext ctx, final IInputObject inputPayload) throws Exception {
				rec.parse(inputPayload, forInsert, ctx, null, 0);
				if (ctx.allOk()) {
					logger.info("Service " + sn + " succeeded in parsing input. Same is set as response");
					ctx.setAsResponse(rec);
					return;
				}
				logger.error("Validation failed for service {} and operation {}", sn, operation.name());
			}

			@Override
			public String getId() {
				return sn;
			}
		};
	}

	protected abstract class Service implements IService {
		private final String serviceName;

		protected Service(final String name) {
			this.serviceName = name;
		}

		@Override
		public String getId() {
			return this.serviceName;
		}
	}

	protected class Reader extends Service {

		protected Reader(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			if (!Form.this.parseKeys(payload, ctx)) {
				logger.error("Error while reading keys from the input payload");
				return;
			}

			final DbRecord rec = (DbRecord) Form.this.record;
			RdbDriver.getDriver().read(handle -> {
				if (!rec.read(handle)) {
					logger.error("No data found for the requested keys");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return;
				}
				/*
				 * instead of storing data and then serializing it, we have
				 * designed this service to serialize data then-and-there
				 */
				final ISerializer writer = ctx.getSerializer();
				writer.beginObject();
				writer.fields(rec);

				for (final LinkedForm<?> link : Form.this.linkedForms) {
					link.read(rec, writer, handle);
				}
				writer.endObject();
			});
		}
	}

	protected class Creater extends Service {

		protected Creater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parse(payload, true, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}
			RdbDriver.getDriver().readWrite(handle -> {
				if (!rec.insert(handle)) {
					logger.error("Insert operation failed silently");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}
				for (final LinkedForm<?> lf : Form.this.linkedForms) {
					if (!lf.insert(rec, payload, handle, ctx)) {
						logger.error("Insert operation failed for linked form");
						ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
						return false;
					}
				}
				return true;
			});
		}
	}

	protected class Updater extends Service {

		protected Updater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parse(payload, false, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}

			RdbDriver.getDriver().readWrite(handle -> {
				if (!rec.update(handle)) {
					logger.error("update operation failed silently");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}
				for (final LinkedForm<?> lf : Form.this.linkedForms) {
					if (!lf.update(rec, payload, handle, ctx)) {
						logger.error("Update operation failed for linked form");
						ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
						return false;

					}
				}
				return true;
			});
		}
	}

	protected class Deleter extends Service {

		protected Deleter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while validating keys");
				return;
			}

			RdbDriver.getDriver().readWrite(handle -> {
				if (!rec.delete(handle)) {
					logger.error("Delete operation failed silently");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}

				for (final LinkedForm<?> lf : Form.this.linkedForms) {
					if (!lf.delete(rec, handle, ctx)) {
						logger.error("Insert operation failed for linked form");
						ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
						return false;
					}
				}
				return true;
			});
		}
	}

	protected class Filter extends Service {

		protected Filter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			final ParsedFilter filter = rec.dba.parseFilter(payload, ctx);

			if (filter == null) {
				logger.error("Error while parsing filter conditions from th einput payload");
				return;
			}

			RdbDriver.getDriver().read(handle -> {
				final List<Object[]> list = rec.dba.filter(filter.getWhereClause(), filter.getWhereParamValues(),
						handle);
				/*
				 * instead of storing data and then serializing it, we have
				 * designed this service to serialize data then-and-there
				 */
				final ISerializer writer = ctx.getSerializer();
				writer.beginObject();
				writer.name(Conventions.Http.TAG_LIST);
				writer.beginArray();

				if (list.size() == 0) {
					logger.warn("No rows filtered. Responding with empty list");
				} else {
					for (final Object[] row : list) {
						final DbRecord r = rec.newInstance(row);
						writer.beginObject();
						writer.fields(r);
						for (final LinkedForm<?> link : Form.this.linkedForms) {
							link.read(r, writer, handle);
						}
						writer.endObject();
					}
				}
				writer.endArray();
				writer.endObject();
			});
		}
	}
}
