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

package org.simplity.fm.upload;

import java.sql.SQLException;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.service.IServiceContext;

/**
 *
 * @author simplity.org
 *
 */
class FormLoader {
	private final Schema form;
	/**
	 * if non-null, generated key is copied to the value with this name
	 */
	private final String generatedKeyOutputName;

	/**
	 * array of value providers corresponding to the fields in this form
	 */
	private final IValueProvider[] valueProviders;

	private final int keyIdx;

	/**
	 *
	 * @param form
	 *            to be used for inserting row
	 * @param generatedKeyOutputName
	 *            if the table has generated key, and the generated key is to be
	 *            used by another form, then this is the name of the field with
	 *            which this generated key is put back into the values map
	 * @param valueProviders
	 *            must have exactly the right number and in the same order for
	 *            the form fields
	 */
	FormLoader(final Schema form, final String generatedKeyOutputName, final IValueProvider[] valueProviders) {
		this.form = form;
		this.generatedKeyOutputName = generatedKeyOutputName;
		this.valueProviders = valueProviders;
		if (this.generatedKeyOutputName == null) {
			this.keyIdx = -1;
		} else {
			this.keyIdx = this.form.getKeyIndexes()[0];
		}
	}

	/**
	 * validate data
	 *
	 * @param values
	 * @param ctx
	 *            that must have user and tenantKey if the insert operation
	 *            require these. errors, if any are added to this.
	 * @return true of all ok. false otherwise, in which case ctx will have the
	 *         errors
	 */
	boolean validate(final Map<String, String> values, final IServiceContext ctx) {
		return this.parseInput(values, ctx) != null;

	}

	private SchemaData parseInput(final Map<String, String> values, final IServiceContext ctx) {
		final String[] data = new String[this.valueProviders.length];
		int idx = -1;
		for (final IValueProvider vp : this.valueProviders) {
			idx++;
			if (vp != null) {
				data[idx] = vp.getValue(values, ctx);
			}
		}

		final int nbrExistingErrors = ctx.getNbrErrors();
		final SchemaData dataRow = this.form.parseForInsert(data, ctx);
		final int nbrErrors = ctx.getNbrErrors();
		if (nbrErrors > nbrExistingErrors) {
			return null;
		}
		return dataRow;
	}

	/**
	 *
	 * @param values
	 * @param ctx
	 *            that must have user and tenantKey if the insert operation
	 *            require these. errors, if any are added to this.
	 * @return true of all ok. false otherwise, in which case ctx will have the
	 *         errors
	 * @throws SQLException
	 */
	boolean loadData(final Map<String, String> values, final DbHandle handle, final IServiceContext ctx)
			throws SQLException {
		final SchemaData fd = this.parseInput(values, ctx);
		if (fd == null) {
			return false;
		}

		if (!fd.insert(handle)) {
			ctx.addMessage(Message.newError("Row not inserted, probably because of database constraints"));
			return false;
		}

		if (this.generatedKeyOutputName != null) {
			final Object key = fd.getValue(this.keyIdx);
			if (key != null) {
				values.put(this.generatedKeyOutputName, key.toString());
			}
		}
		return true;
	}

}
