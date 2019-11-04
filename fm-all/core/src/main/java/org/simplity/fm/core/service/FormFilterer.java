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

package org.simplity.fm.core.service;

import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.FilterSql;
import org.simplity.fm.core.rdb.RdbDriver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public class FormFilterer extends FormOperator {

	/**
	 *
	 * @param form
	 */
	public FormFilterer(final Form form) {
		this.form = form;
		this.ioType = IoType.FILTER;
	}

	@Override
	public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
		logger.info("Started filtering form {}", this.form.getFormId());
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

		final FilterSql reader = this.form.getSchema().parseForFilter(conditions, sorts, msgs, ctx, nbrRows);

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

		final DataTable tbl = new DataTable(this.form.getSchema());
		RdbDriver.getDriver().transact(handle -> {
			tbl.fetch(handle, reader);
			return true;
		}, true);

		logger.info(" {} rows filtered", tbl.length());

		try (JsonWriter writer = new JsonWriter(ctx.getResponseWriter())) {
			writer.beginObject();
			writer.name(Conventions.Http.TAG_LIST);
			tbl.serializeAsJson(writer);
			writer.endObject();
		}
	}
}
