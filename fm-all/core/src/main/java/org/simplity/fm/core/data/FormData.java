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
import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.datatypes.ValueType;

import com.google.gson.stream.JsonWriter;

/**
 * <p>
 * Abstract representation of data associated with a form. Each Concrete class
 * of Form will has an associated concrete class of FormData.
 * </p>
 *
 * <p>
 * This is a wrapper on DataObject to accommodate values for local fields if
 * any. While this base class defines an array of objects to hold all local
 * field values, generated concrete classes define setters and getters for
 * specific fields
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class FormData {

	protected final Form form;
	/**
	 * data for the schema that this form is based on
	 */
	protected SchemaData dataObject;
	/**
	 * data for local fields that this form may have. null if there are no local
	 * fields.
	 */
	protected final Object[] fieldValues;

	/**
	 * form data for all linked forms
	 */
	protected final FormDataTable[] linkedData;

	/**
	 * delegated back by the concrete class to ensure that only the data
	 * components are right
	 *
	 */
	protected FormData(final Form form, final SchemaData dataObject, final Object[] fieldValues,
			final FormDataTable[] linkedData) {
		this.form = form;
		if (dataObject == null) {
			this.dataObject = this.form.getSchema().newSchemaData();
		} else {
			this.dataObject = dataObject;
		}
		this.fieldValues = fieldValues;
		this.linkedData = linkedData;
	}

	protected Form getForm() {
		return this.form;
	}

	/*
	 * called by generated concrete class, and never by end-programmers.
	 */
	protected SchemaData getDataObject() {
		return this.dataObject;
	}

	protected Object getObject(final int index) {
		if (this.fieldValues == null) {
			return null;
		}
		return this.fieldValues[index];
	}

	protected void setObject(final int index, final Object value) {
		this.fieldValues[index] = value;
	}

	protected Object[] getFieldValues() {
		if (this.fieldValues == null) {
			return null;
		}
		return this.fieldValues;
	}

	protected long getLongValue(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}
		try {
			return Long.parseLong(obj.toString());
		} catch (final Exception e) {
			//
		}
		return 0;
	}

	protected String getStringValue(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}

	protected LocalDate getDateValue(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj == null) {
			return null;
		}
		if (obj instanceof LocalDate) {
			return (LocalDate) obj;
		}
		try {
			return LocalDate.parse(obj.toString());
		} catch (final Exception e) {
			//
		}
		return null;
	}

	protected boolean getBoolValue(final int idx) {
		Object obj = this.getObject(idx);
		if (obj == null) {
			return false;
		}
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		obj = ValueType.Boolean.parse(obj.toString());
		if (obj instanceof Boolean) {
			return (boolean) obj;
		}
		return false;
	}

	protected double getDecimalValue(final int idx) {
		final Object obj = this.getObject(idx);

		if (obj == null) {
			return 0;
		}

		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		}

		try {
			Double.parseDouble(obj.toString());
		} catch (final Exception e) {
			//
		}
		return 0;
	}

	protected Instant getTimestampValue(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj == null) {
			return null;
		}
		if (obj instanceof Instant) {
			return (Instant) obj;
		}
		if (obj instanceof String) {

			try {
				return Instant.parse(obj.toString());
			} catch (final Exception e) {
				//
			}
		}
		return null;
	}

	protected FormDataTable[] getLinkedData() {
		return this.linkedData;
	}

	protected FormDataTable getLinkedData(final int idx) {
		return this.linkedData[idx];
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeFields(final JsonWriter writer) throws IOException {
		if (this.dataObject != null) {
			this.dataObject.serializeFields(writer);
		}

		if (this.form.localFields != null && this.fieldValues != null) {
			JsonUtil.writeFields(this.form.localFields, this.fieldValues, writer);
		}

		if (this.linkedData != null) {
			int idx = -1;
			for (final LinkedForm lf : this.form.linkedForms) {
				idx++;
				final FormDataTable table = this.linkedData[idx];
				if (table != null) {
					writer.name(lf.linkName);
					table.serializeRows(writer);
				}
			}
		}
	}

	/**
	 *
	 * @return schema data associate with this form data. COuld be null
	 */
	public SchemaData getSchemaData() {
		return this.dataObject;
	}
}
