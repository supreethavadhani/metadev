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
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * represents a schema for input from a client or output to a client
 *
 * @author simplity.org
 *
 */
public class Form {
	static protected final Logger logger = LoggerFactory.getLogger(Form.class);

	protected String name;
	protected Schema schema;
	/*
	 * db operations that are to be exposed thru this form. array corresponds to
	 * the ordinals of IoType
	 */
	protected boolean[] operations;

	/**
	 *
	 * @return unique name of this form
	 */
	public String getFormId() {
		return this.name;
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void fetch(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = Form.this.getSchema().parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.fetch(handle);
			return true;
		}, true);

		if (result[0]) {
			try {
				dataRow.serializeAsJson(ctx.getResponseWriter());
			} catch (final IOException e) {
				final String msg = "I/O error while serializing e=" + e + ". message=" + e.getMessage();
				logger.error(msg);
				ctx.addMessage(Message.newError(msg));
			}
		} else {
			logger.error("No data found for the requested keys");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void update(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = Form.this.getSchema().parseRow(payload, false, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.update(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not updated, possibly because of time-stamp issues");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void insert(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = Form.this.getSchema().parseRow(payload, true, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.insert(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not inserted, possibly because of issues with key");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void filter(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final List<Message> msgs = new ArrayList<>();
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

		final FilterSql reader = Form.this.schema.parseForFilter(conditions, sorts, msgs, ctx, nbrRows);

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

		final DataTable dataTable = new DataTable(Form.this.schema);
		RdbDriver.getDriver().transact(handle -> {
			dataTable.fetch(handle, reader);
			return true;
		}, true);

		logger.info(" {} rows filtered", dataTable.length());

		try (JsonWriter writer = new JsonWriter(ctx.getResponseWriter())) {
			writer.beginObject();
			writer.name(Conventions.Http.TAG_LIST);
			dataTable.serializeAsJson(writer);
			writer.endObject();
		}
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void delete(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = Form.this.schema.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.deleteFromDb(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not deleted. Key issues?");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void bulkUpdate(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final JsonArray arr = payload.getAsJsonArray(Conventions.Http.TAG_LIST);
		if (arr == null || arr.size() == 0) {
			logger.error("No data or data is empty");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
			return;
		}

		final DataTable dataTable = Form.this.getSchema().parseTable(arr, true, ctx, null);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}

		RdbDriver.getDriver().transact(handle -> {
			final boolean ok = dataTable.save(handle);
			if (!ok) {
				logger.error("Error while saving rows into the DB. Operation abandoned and transaction is rolled back");
				ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
				return false;
			}
			logger.info("Data table saved all rows");
			return true;
		}, false);
	}

	/**
	 * get the service instance for the desired operation on this form
	 *
	 * @param opern
	 * @return service, or null if this form is not designed for this operation
	 */
	public IService getService(final IoType opern) {
		if (this.operations[opern.ordinal()] == false) {
			return null;
		}

		switch (opern) {
		case GET:
			return new FormService(IoType.GET) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.fetch(ctx, inputPayload);

				}
			};

		case FILTER:
			return new FormService(IoType.FILTER) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.filter(ctx, inputPayload);

				}
			};

		case CREATE:
			return new FormService(IoType.CREATE) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.insert(ctx, inputPayload);

				}
			};

		case UPDATE:
			return new FormService(IoType.UPDATE) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.update(ctx, inputPayload);

				}
			};

		case BULK:
			return new FormService(IoType.BULK) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Form.this.bulkUpdate(ctx, inputPayload);

				}
			};

		case DELETE:
			return new FormService(IoType.DELETE) {

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

}
