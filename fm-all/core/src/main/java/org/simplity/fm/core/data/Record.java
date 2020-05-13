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
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * <p>
 * Represents a set of field-value pairs. This is one of the core classes.
 * </p>
 * <p>
 * We have chosen an array of objects as the data-structure for field values. Of
 * course, the fields are not in any order, but we have have chosen an array
 * over map for ease of access with a generated meta-data for fields that
 * include their index in the array
 * </p>
 * <p>
 * While such an approach is quite useful for the framework to carry out its job
 * of auto-plumbing data between the client and the DB,it is quite painful for a
 * programmer to write custom code around such an API that requires array index.
 * For example setLongValue(int, long) is error prone, even if we provide static
 * literals for the index (like Customer.ID).Hence
 * </p>
 * <p>
 * It is expected that this class is used only by utility classes. We provide
 * code-generation utilities for a project to generate extended classes based on
 * meta-data provided for the fields
 * </p>
 *
 * @author simplity.org
 *
 *
 *
 */
public abstract class Record {
	private static final Logger logger = LoggerFactory.getLogger(Record.class);

	/**
	 * fields that make up this record. These fields to bo
	 */
	protected final Field[] fields;

	/**
	 * current
	 *
	 */
	protected final Object[] fieldValues;
	/**
	 * describes all the inter-field validations, and any business validations
	 */
	protected IValidation[] validations;

	/**
	 * construct this record with a set of fields and values
	 *
	 * @param fields
	 *            non-null non-empty array of fields
	 * @param fieldValues
	 *            null to create a record with default values. if non-null, then
	 *            the array MUST contain the right number and type of elements
	 */
	protected Record(final Field[] fields, final Object[] fieldValues) {
		this.fields = fields;
		if (fieldValues == null) {
			final int nbr = fields.length;

			this.fieldValues = new Object[nbr];
			for (int i = 0; i < fields.length; i++) {
				this.fieldValues[i] = this.fields[i].getDefaultValue();
			}
			return;
		}
		if (fieldValues.length != fields.length) {
			throw new RuntimeException("record is being instantiated with " + fields.length + " fields but "
					+ fieldValues.length + " values.");
		}
		this.fieldValues = fieldValues;
	}

	/**
	 *
	 * @return unique name assigned to this record. concrete classes are
	 *         generally generated from meta data files, and this name is based
	 *         on the conventions used in the app
	 */
	public abstract String getName();

	/**
	 * @return the validations
	 */
	public IValidation[] getValidations() {
		return this.validations;
	}

	/**
	 * @return number of columns in this table
	 */
	public int getNbrFields() {
		return this.fields.length;
	}

	/**
	 * @return the fields
	 */
	public Field[] getFields() {
		return this.fields;
	}

	/**
	 * set value for a field at the specified 0-based field index. Value is
	 * silently ignored if the index is out of range
	 *
	 * @param idx
	 *            must be a valid index, failing which the operation is ignored
	 * @param value
	 *            MUST be one of the standard instances viz: String, Long,
	 *            Double, Boolean, LocalData, Instant
	 */
	public void setValue(final int idx, final Object value) {
		try {
			this.fieldValues[idx] = value;
		} catch (final Exception e) {
			this.logError(idx);
		}
	}

	/**
	 * get value of a field at the specified 0-based field index. Null is
	 * returned if the index is out of range
	 *
	 * @param idx
	 *            must be a valid index, failing which null is returned
	 * @return
	 *         null if the index is invalid, or the value is null. Otherwise one
	 *         of the standard instances viz: String, Long,
	 *         Double, Boolean, LocalData, Instant
	 */
	public Object getValue(final int idx) {
		try {
			return this.fieldValues[idx];
		} catch (final Exception e) {
			this.logError(idx);
			return null;
		}
	}

	/**
	 * @return field values
	 */
	public Object[] getRawData() {
		return this.fieldValues;
	}

	/**
	 *
	 * @param idx
	 * @return get value at this index as long. 0 if the index is not valid, or
	 *         the value is not long
	 */
	protected long getLongValue(final int idx) {
		final Object obj = this.getValue(idx);
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

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of integer type. false otherwise,
	 *         and the value is not set
	 */
	protected boolean setLongValue(final int idx, final long value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}
		final ValueType vt = field.getValueType();

		if (vt == ValueType.Integer) {
			this.fieldValues[idx] = value;
			return true;
		}

		if (vt == ValueType.Decimal) {
			final double d = value;
			this.fieldValues[idx] = d;
			return true;
		}

		if (vt == ValueType.Text) {

			this.fieldValues[idx] = "" + value;
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param idx
	 * @return value of the field as text. null if no such field, or the field
	 *         has null value. toString() of object if it is non-string
	 */
	protected String getStringValue(final int idx) {
		final Object obj = this.getValue(idx);
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of String type. false otherwise, and
	 *         the value is not set
	 */
	protected boolean setStringValue(final int idx, final String value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}
		final ValueType vt = field.getValueType();

		if (vt == ValueType.Text) {
			this.fieldValues[idx] = value;
			return true;
		}

		final Object obj = vt.parse(value);

		if (obj != null) {
			this.fieldValues[idx] = obj;
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param idx
	 * @return value of the field as Date. null if the field is not a date
	 *         field, or it has null value
	 */
	protected LocalDate getDateValue(final int idx) {
		final Object obj = this.getValue(idx);
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

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of Date type. false otherwise, and
	 *         the value is not set
	 */
	protected boolean setDateValue(final int idx, final LocalDate value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}

		final ValueType vt = field.getValueType();

		if (vt == ValueType.Date) {
			this.fieldValues[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.fieldValues[idx] = value.toString();
			return true;
		}

		return false;
	}

	/**
	 *
	 * @return value of the field as boolean. false if no such field, or the
	 * @param idx
	 *            field is null,or the field is not boolean.
	 */
	protected boolean getBoolValue(final int idx) {
		Object obj = this.getValue(idx);
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

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of boolean type. false otherwise,
	 *         and the value is not set
	 */
	protected boolean setBoolValue(final int idx, final boolean value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}

		final ValueType vt = field.getValueType();

		if (vt == ValueType.Boolean) {
			this.fieldValues[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.fieldValues[idx] = "" + value;
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param idx
	 * @return value of the field if it decimal. 0 index is invalid or the value
	 *         is not double/decimal.
	 */
	protected double getDecimalValue(final int idx) {
		final Object obj = this.getValue(idx);

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

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of double type. false otherwise,
	 *         and the value is not set
	 */
	protected boolean setDecimlValue(final int idx, final double value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}

		final ValueType vt = field.getValueType();

		if (vt == ValueType.Decimal) {
			this.fieldValues[idx] = value;
			return true;
		}

		if (vt == ValueType.Integer) {
			this.fieldValues[idx] = ((Number) value).longValue();
			return true;
		}

		if (vt == ValueType.Text) {
			this.fieldValues[idx] = "" + value;
			return true;
		}

		return false;
	}

	/**
	 * Note that this is NOT LocalDateTime. It is instant. We do not deal with
	 * localDateTime as of now.
	 *
	 * @param idx
	 * @return value of the field as instant of time. null if the field is not
	 *         an instant.
	 *         field, or it has null value
	 */
	protected Instant getTimestampValue(final int idx) {
		final Object obj = this.getValue(idx);
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

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *
	 * @return true if field exists, and is of Instant type. false otherwise,
	 *         and the value is not set
	 */
	protected boolean setTimestampValue(final int idx, final Instant value) {
		final Field field = this.fields[idx];
		if (field == null) {
			return false;
		}

		final ValueType vt = field.getValueType();

		if (vt == ValueType.Timestamp) {
			this.fieldValues[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.fieldValues[idx] = value.toString();
			return true;
		}

		return false;
	}

	private void logError(final int idx) {
		logger.error("Invalid index {} used for setting value in a record with {} values", idx, this.fields.length);
	}

	/**
	 * parse this record from a serialized input
	 *
	 * @param json
	 * @param ctx
	 * @param tableName
	 * @param rowNbr
	 * @return true if all ok. false if any parse error is added to the context
	 */
	public boolean parse(final JsonObject json, final IServiceContext ctx, final String tableName, final int rowNbr) {
		boolean ok = true;
		for (final Field field : this.fields) {
			final String value = JsonUtil.getString(json, field.getName());
			if (!field.parseIntoRow(value, this.fieldValues, ctx, tableName, rowNbr)) {
				ok = false;
			}
		}

		if (this.validations == null) {
			for (final IValidation vln : this.validations) {
				if (vln.isValid(this, ctx) == false) {
					logger.error("field {} failed an inter-field validaiton associated with it", vln.getFieldName());
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeFields(final JsonWriter writer) throws IOException {
		JsonUtil.writeFields(this.fields, this.fieldValues, writer);
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	protected void serializeRows(final JsonWriter writer, final Object[][] rows) throws IOException {
		JsonUtil.writeRows(this.fields, rows, writer);
	}

	/**
	 * make a copy of this record.
	 *
	 * @return a copy of this that can be mutilated without affecting this
	 */
	public Record makeACopy() {
		return this.newInstance(this.fieldValues);
	}

	/**
	 *
	 * @return a new instance of this record. Used by utilities where doing a
	 *         new class() is not possible;
	 */
	public Record newInstance() {
		return this.newInstance(null);
	}

	/**
	 * make a copy of this record
	 *
	 * @return a copy of this that can be mutilated without affecting this
	 */
	protected abstract Record newInstance(Object[] values);
}
