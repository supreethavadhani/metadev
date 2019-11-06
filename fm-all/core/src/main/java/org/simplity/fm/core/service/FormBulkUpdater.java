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

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.rdb.RdbDriver;

import com.google.gson.JsonArray;
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
		this.ioType = IoType.BULK;
	}

	@Override
	public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final JsonArray arr = payload.getAsJsonArray(Conventions.Http.TAG_LIST);
		if (arr == null || arr.size() == 0) {
			logger.error("No data or data is empty");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
			return;
		}

		final DataTable dataTable = this.form.getSchema().parseTable(arr, true, ctx, null);
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
}
