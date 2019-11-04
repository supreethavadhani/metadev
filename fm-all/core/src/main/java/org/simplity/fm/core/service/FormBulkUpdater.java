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

import java.io.IOException;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.DataRow;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.rdb.RdbDriver;

import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class FormBulkUpdater extends FormOperator {

	/**
	 *
	 * @param form
	 */
	public FormBulkUpdater(final Form form) {
		this.form = form;
		this.ioType = IoType.GET;
	}

	@Override
	public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final DataRow dataRow = this.form.schema.parseKeys(payload, ctx);
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
}
