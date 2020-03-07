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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public abstract class FormDataTable implements Iterable<FormData> {
	private static final Logger logger = LoggerFactory.getLogger(FormDataTable.class);

	protected final Form form;
	protected SchemaDataTable dataTable;
	protected final Object[][] fieldValues;
	protected final FormDataTable[][] linkedData;

	protected FormDataTable(final Form form, final SchemaDataTable dataTable, final Object[][] fieldValues,
			final FormDataTable[][] linkedData) {
		this.form = form;

		/*
		 * ensure that the schema matches before accepting data
		 */
		final Schema sch = form.schema;

		if (sch == null) {
			this.dataTable = null;
		} else if (dataTable == null) {
			this.dataTable = sch.newSchemaDataTable();
		} else if (sch.name.equals(dataTable.schema.name)) {
			this.dataTable = dataTable;
		} else {
			throw new IllegalArgumentException("FormData " + this.form.name + " uses schema " + sch.name
					+ " but data created for schema " + dataTable.schema.name + " is provided");
		}

		/*
		 * local fields, if present, must have at least the right number of
		 * elements
		 */
		if (this.form.localFields == null || fieldValues == null) {
			this.fieldValues = null;
		} else {
			if (this.form.localFields.length != fieldValues.length) {
				throw new IllegalArgumentException("Form " + this.form.name + " has " + this.form.localFields.length
						+ " local fields but " + fieldValues.length + " values are provided");
			}
			this.fieldValues = fieldValues;
		}

		/*
		 * we check only for this first dimension..
		 */
		if (this.dataTable == null || this.form.linkedForms == null || linkedData == null) {
			this.linkedData = null;
		} else {
			if (this.dataTable.length() != linkedData.length) {
				throw new IllegalArgumentException("Form data table is assigned dataTable of " + this.dataTable.length()
						+ " elements but linked data is provided for " + this.form.linkedForms.length + " elements");
			}
			this.linkedData = linkedData;
		}
	}

	/**
	 *
	 * @return data table associated with this form table
	 */
	public SchemaDataTable getDataTable() {
		return this.dataTable;
	}

	/**
	 *
	 * @return local field values associated with this form table
	 */
	public Object[][] getFiedValues() {
		return this.fieldValues;
	}

	/**
	 * iterator for data rows
	 */
	@Override
	public Iterator<FormData> iterator() {
		final int nbr = this.dataTable.length();
		return new Iterator<FormData>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < nbr;
			}

			@Override
			public FormData next() {
				return FormDataTable.this.getFormData(this.idx++);
			}
		};
	}

	/**
	 * @param idx
	 * @return form data at the index. null if no data at that index
	 */
	protected FormData getFormData(final int idx) {
		try {
			Object[] vals = null;
			FormDataTable[] link = null;
			if (this.fieldValues != null) {
				vals = this.fieldValues[idx];
			}

			if (this.linkedData != null) {
				link = this.linkedData[idx];
			}

			return this.form.newFormData(this.dataTable.getSchemaData(idx), vals, link);
		} catch (final ArrayIndexOutOfBoundsException e) {
			logger.error("Form data table has " + this.dataTable.length() + " elements, but a request for " + idx
					+ " is received", e);
			return null;
		}
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeRows(final JsonWriter writer) throws IOException {
		writer.beginArray();
		final int nbr = this.length();
		for (int i = 0; i < nbr; i++) {
			final FormData fd = this.getFormData(i);
			writer.beginObject();
			fd.serializeFields(writer);
			writer.endObject();
		}
		writer.endArray();
	}

	/**
	 * @return number of rows in this data table
	 */
	public int length() {
		if (this.dataTable == null) {
			return 0;
		}
		return this.dataTable.length();
	}
}
