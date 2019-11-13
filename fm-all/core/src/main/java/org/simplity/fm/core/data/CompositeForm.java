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
import org.simplity.fm.core.service.IServiceContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * represents a schema for input from a client or output to a client
 *
 * @author simplity.org
 *
 */
public class CompositeForm extends Form {
	protected LinkedForm[] linkedForms;

	/**
	 *
	 * @return unique name of this form
	 */
	@Override
	public String getFormId() {
		return this.name;
	}

	/**
	 * @return the schema
	 */
	@Override
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
		final DataRow dataRow = this.getSchema().parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final int nbrLinks = this.linkedForms.length;
		final DataTable[] childData = new DataTable[nbrLinks];
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			final boolean ok = dataRow.fetch(handle);
			if (ok) {
				int idx = -1;
				for (final LinkedForm lf : this.linkedForms) {
					idx++;
					childData[idx] = lf.fetch(handle, dataRow.dataRow);
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

			this.schema.serializeToJson(dataRow.dataRow, jw);
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
		this.writeWorker(ctx, payload, false);
	}

	/**
	 * insert the data that has come from a client into the database
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void insert(final IServiceContext ctx, final JsonObject payload) throws Exception {
		this.writeWorker(ctx, payload, true);
	}

	protected void writeWorker(final IServiceContext ctx, final JsonObject payload, final boolean forInsert)
			throws Exception {
		final DataRow dataRow = this.getSchema().parseRow(payload, forInsert, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}

		final int nbrLinks = this.linkedForms.length;
		final DataTable[] childData = new DataTable[nbrLinks];
		int i = -1;
		for (final LinkedForm lf : this.linkedForms) {
			i++;
			final JsonArray arr = payload.getAsJsonArray(lf.linkName);
			childData[i] = lf.parse(arr, forInsert, ctx);
		}

		if (!ctx.allOk()) {
			logger.error("Error while reading data for child-forms");
			return;
		}

		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			boolean ok = false;
			if (forInsert) {
				ok = dataRow.insert(handle);
			} else {
				ok = dataRow.update(handle);
			}

			if (!ok) {
				return false;
			}
			int idx = -1;
			for (final LinkedForm lf : this.linkedForms) {
				idx++;
				if (forInsert) {
					ok = lf.insert(handle, childData[idx], dataRow.dataRow);
				} else {
					ok = lf.save(handle, childData[idx], dataRow.dataRow);
				}
				if (!ok) {
					return false;
				}
			}
			result[0] = true;

			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not updated, possibly because of time-stamp issues");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * delete rows from the database based on the data sent from a client
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void delete(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = this.getSchema().parseKeys(payload, ctx);
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

			for (final LinkedForm lf : this.linkedForms) {
				ok = lf.delete(handle, dataRow.dataRow);
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

	protected class FormReader extends FormService {
		protected FormReader() {
			this.opern = IoType.GET;
		}

		@Override
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
			CompositeForm.this.fetch(ctx, payload);
		}
	}

	protected class FormUpdater extends FormService {
		public FormUpdater() {
			this.opern = IoType.UPDATE;
		}

		@Override
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
			CompositeForm.this.writeWorker(ctx, payload, false);
		}
	}

	protected class FormInserter extends FormService {
		protected FormInserter() {
			this.opern = IoType.CREATE;
		}

		@Override
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
			CompositeForm.this.writeWorker(ctx, payload, true);
		}
	}

	protected class FormDeleter extends FormService {
		public FormDeleter() {
			this.opern = IoType.DELETE;
		}

		@Override
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
			CompositeForm.this.delete(ctx, payload);
		}
	}
}
