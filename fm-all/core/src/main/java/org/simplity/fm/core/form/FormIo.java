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

package org.simplity.fm.core.form;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.IDbClient;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * service for a form based I/O operation from DB
 * 
 * @author simplity.org
 *
 */
public abstract class FormIo implements IService {
	protected static final Logger logger = LoggerFactory.getLogger(FormIo.class);

	/**
	 * 
	 * @param opern
	 * @param formName
	 * @return non-null instance
	 */
	public static FormIo getInstance(IoType opern, String formName) {
		Form form = ComponentProvider.getProvider().getForm(formName);
		if (form == null) {
			logger.error("No form named {}.", formName);
			return null;
		}
		DbMetaData meta = form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for any db operation.", formName);
			return null;
		}

		if (meta.dbOperationOk[opern.ordinal()] == false) {
			logger.error("Form {} is not designed for db operation.", formName, opern);
			return null;
		}

		switch (opern) {
		case CREATE:
			return new FormInserter(form);

		case DELETE:
			return new FormDeleter(form);

		case FILTER:
			return new FormFilter(form);

		case GET:
			return new FormReader(form);

		case UPDATE:
			return new FormUpdater(form);

		case BULK:
			return new BulkUpdater(form);

		default:
			logger.error("Form operation {} not yet implemented", opern);
			return null;
		}
	}

	protected static String toServiceName(Form form, IoType oper) {
		return oper.name() + '-' + form.getFormId();
	}

	protected static class FormReader extends FormIo {
		private final Form form;

		protected FormReader(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.GET);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			FormData fd = this.form.newFormData();
			Field tenant = this.form.dbMetaData.tenantField;
			if (tenant != null) {
				fd.setObject(tenant.getIndex(), ctx.getTenantId());
			}

			/*
			 * read by unique keys?
			 */
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					boolean ok = fd.loadUniqKeys(payload);
					if (ok) {
						ok = fd.fetchUsingUniqueKeys(handle);
					} else {
						// try primary keys
						fd.loadKeys(payload, ctx);
						if (!ctx.allOk()) {
							return true;
						}
						ok = fd.fetch(handle);
					}
					if (ok) {
						try {
							fd.serializeAsJson(ctx.getResponseWriter());
						} catch (IOException e) {
							String msg = "I/O error while serializing e=" + e + ". message=" + e.getMessage();
							logger.error(msg);
							ctx.addMessage(Message.newError(msg));
						}
					} else {
						logger.error("No data found");
						ctx.addMessage(Message.newError("noData"));
					}
					return true;
				}
			}, true);
			return;
		}
	}

	protected static class FormFilter extends FormIo {
		private final Form form;

		protected FormFilter(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.FILTER);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			logger.info("Startedfiltering form {}", this.form.uniqueName);
			List<Message> msgs = new ArrayList<>();
			JsonObject conditions = null;
			JsonElement node = payload.get(Conventions.Http.TAG_CONDITIONS);
			if (node != null && node.isJsonObject()) {
				conditions = (JsonObject) node;
			} else {
				logger.error("payload for filter should have attribute named {} to contain conditions",
						Conventions.Http.TAG_CONDITIONS);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return;
			}

			/*
			 * sort order
			 */
			JsonObject sorts = null;
			node = payload.get(Conventions.Http.TAG_SORT);
			if (node != null && node.isJsonObject()) {
				sorts = (JsonObject) node;
			}

			int nbrRows = Conventions.Http.DEFAULT_NBR_ROWS;
			node = payload.get(Conventions.Http.TAG_MAX_ROWS);
			if (node != null && node.isJsonPrimitive()) {
				nbrRows = node.getAsInt();
			}

			SqlReader reader = this.form.parseForFilter(conditions, sorts, msgs, ctx, nbrRows);

			if (msgs.size() > 0) {
				logger.warn("Filering aborted due to errors in nuput data");
				ctx.addMessages(msgs);
				return;
			}

			if (reader == null) {
				logger.error("DESIGN ERROR: form.parseForFilter() returned null, but failed to put ay error message. ");
				ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
				return;
			}

			FormData[][] data = new FormData[1][];
			Form f = this.form;
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					data[0] = FormData.fetchDataWorker(handle, f, reader.sql, reader.whereValues, reader.whereParams,
							f.dbMetaData.selectParams);
					return true;
				}
			}, true);
			FormData[] rows = data[0];
			if (rows == null || rows.length == 0) {
				logger.info("No data found for the form {}", this.form.getFormId());
				rows = new FormData[0];
			} else {
				logger.info(" {} rows filtered", rows.length);
			}
			@SuppressWarnings("resource")
			Writer writer = ctx.getResponseWriter();

			writer.write("{\"");
			writer.write(Conventions.Http.TAG_LIST);
			writer.write("\":[");
			boolean firstOne = true;
			for (FormData fd : rows) {
				if (firstOne) {
					firstOne = false;
				} else {
					writer.write(',');
				}
				fd.serializeAsJson(writer);
			}
			writer.write("]}");
		}
	}

	protected static class FormUpdater extends FormIo {
		private final Form form;

		protected FormUpdater(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.UPDATE);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			FormData fd = this.form.newFormData();
			update(fd, ctx, payload, null);
		}
	}

	protected static class FormInserter extends FormIo {
		private final Form form;

		protected FormInserter(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.CREATE);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			FormData fd = this.form.newFormData();
			insert(fd, ctx, payload, null);
		}

	}

	protected static class FormDeleter extends FormIo {
		private final Form form;

		protected FormDeleter(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.DELETE);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			FormData fd = this.form.newFormData();
			fd.loadKeys(payload, ctx);
			if (!ctx.allOk()) {
				return;
			}
			Field tenant = this.form.dbMetaData.tenantField;
			if (tenant != null) {
				fd.setObject(tenant.getIndex(), ctx.getTenantId());
			}
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					fd.deleteFromDb(handle);
					return true;
				}
			}, false);
			/*
			 * no payload is returned on success
			 */
			return;
		}
	}

	protected static class BulkUpdater extends FormIo {
		protected final Form form;

		protected BulkUpdater(Form form) {
			this.form = form;
		}

		@Override
		public String getId() {
			return toServiceName(this.form, IoType.BULK);
		}

		@Override
		public void serve(IServiceContext ctx, JsonObject payload) throws Exception {
			JsonArray arr = payload.getAsJsonArray(Conventions.Http.TAG_LIST);
			if (arr == null) {
				logger.error("Payload did not contain the required member {}", Conventions.Http.TAG_LIST);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return;
			}

			int nbrRows = arr.size();
			if (nbrRows == 0) {
				logger.error("Payload has no rows to process");
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return;
			}
			logger.info("Started processing {} rows as bulk", nbrRows);
			final BulkWorker worker = new BulkWorker(this.form, ctx);

			//load data into the worker
			arr.forEach(worker);
	
			//push it to db 
			if (ctx.allOk()) {
				RdbDriver.getDriver().transact(worker, false);
			}
		}
	}

	protected static class BulkWorker implements Consumer<JsonElement>, IDbClient {
		private final List<FormData> fds = new ArrayList<>();
		private final List<Boolean> updates = new ArrayList<>();
		private final Form form;
		private final IServiceContext ctx;
		private int tenentIdx;
		private Object tenantValue;

		protected BulkWorker(Form form, IServiceContext ctx) {
			this.form = form;
			this.ctx = ctx;
			Field tenant = this.form.dbMetaData.tenantField;
			if (tenant != null) {
				this.tenantValue = ctx.getTenantId();
				this.tenentIdx = tenant.getIndex();
			}
		}

		@Override
		public void accept(JsonElement ele) {
			if (ele == null || ele instanceof JsonObject == false) {
				logger.error("Bulk row is null or not an object. row ignored.");
				return;
			}
			JsonObject json = (JsonObject) ele;
			FormData fd = this.form.newFormData();
			boolean toUpdate = fd.loadKeys(json, null);
			
			if(this.tenantValue != null) {
				fd.fieldValues[this.tenentIdx] = this.tenantValue;
			}
			
			fd.validateAndLoad(json, false, !toUpdate, this.ctx);
			this.updates.add(toUpdate);
			this.fds.add(fd);
		}
		
		@Override
		public boolean transact(DbHandle handle) throws SQLException {
			if(this.fds.size() == 0) {
				logger.info("Bulk worker has nothing to work on. ");
				return true;
			}
			int nbrInserts = 0;
			int nbrUpdates = 0;
			int idx = -1;
			for(FormData fd :this.fds) {
				idx++;
				if(this.updates.get(idx)) {
					if(fd.update(handle)) {
						logger.info("bulk row {} updated", idx);
						nbrUpdates++;
					}else {
						logger.info("bulk row {} failed to update row{} with folloing data", idx);
						fd.logValues();
					}
				}else {
					if(fd.insert(handle)) {
						logger.info("bulk row {} inserted", idx);
						nbrInserts++;
					}else {
						logger.info("bulk row {} failed to insert with following values", idx);
						fd.logValues();
					}
				}
			}
			logger.info("Bulk Operation: {} rows inserted and {} rows updated", nbrInserts, nbrUpdates);
			return true;
		}
	}

	/**
	 * worker method to allow flexibility in transaction processing.
	 * To make this part of a transaction started by the caller, a non-null
	 * handle is to be passed.
	 * 
	 * If handle is null, this method cmpletes the update on its own
	 * connection
	 */
	protected static void update(FormData fd, IServiceContext ctx, JsonObject payload, DbHandle handle) throws Exception {
		Form form = fd.getForm();
		fd.validateAndLoad(payload, false, false, ctx);
		/*
		 * special case of time-stamp check for updates!!
		 */
		Field f = form.dbMetaData.timestampField;
		if (f != null) {
			Object val = null;
			JsonPrimitive el = payload.getAsJsonPrimitive(f.getFieldName());
			if (el == null) {
				ctx.addMessage(Message.newFieldError(f.getFieldName(), Message.FIELD_REQUIRED));
			} else {
				val = f.parse(el.getAsString());
				if (val == null) {
					ctx.addMessage(Message.newFieldError(f.getFieldName(), Message.INVALID_TIMESTAMP));
				} else {
					fd.setObject(f.getIndex(), val);
				}
			}
		}

		if (!ctx.allOk()) {
			logger.warn("Update operation stopped due to errors in input data");
			return;
		}

		f = form.dbMetaData.tenantField;
		if (f != null) {
			fd.setObject(f.getIndex(), ctx.getTenantId());
		}

		final boolean[] result = new boolean[1];
		if (handle == null) {
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle dbHandle) throws SQLException {
					result[0] = fd.update(dbHandle);
					return true;
				}
			}, false);
		} else {
			result[0] = fd.update(handle);
		}

		if (!result[0]) {
			/*
			 * no update? quite
			 */
			logger.error("This row is updated by another user. Client has to cancel the operation");
			ctx.addMessage(Message.newError(Message.CONCURRENT_UPDATE));
		}
		/*
		 * what should be the payload back? As of now, we send nothing.
		 */
		return;
	}
	static protected void insert(FormData fd, IServiceContext ctx, JsonObject payload, DbHandle handle) throws Exception {
		Form form = fd.getForm();
		fd.validateAndLoad(payload, false, true, ctx);
		if (!ctx.allOk()) {
			return;
		}
		Field tenant = form.dbMetaData.tenantField;
		if (tenant != null) {
			fd.setObject(tenant.getIndex(), ctx.getTenantId());
		}

		if (handle == null) {
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle dbHandle) throws SQLException {
					fd.insert(dbHandle);
					return true;
				}
			}, false);
		} else {
			fd.insert(handle);
		}
		/*
		 * as per our protocol, we send the form back as payload, possibly
		 * because we may have to communicate the generated code back to the
		 * client
		 */
		fd.serializeAsJson(ctx.getResponseWriter());
		return;
	}

}
