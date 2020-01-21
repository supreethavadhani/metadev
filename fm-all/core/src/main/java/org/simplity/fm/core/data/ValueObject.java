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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * Represents a generic Value object that holds value internally in an array. It
 * also has meta data to take care of functionality other than just getters and
 * setters.
 *
 * COncrete classes can be generated based on meta data with named getters and
 * setters
 *
 * @author simplity.org
 *
 */
public class ValueObject {
	private static final Logger logger = LoggerFactory.getLogger(ValueObject.class);
	protected Field[] fields;
	protected Object[] fieldValues;

	/**
	 * create a Vo with fields and possibly initial values
	 *
	 * @param fields
	 *            non-null non-empty array of fields
	 * @param fieldValues
	 *            null to create a VO with default values. if non-null, then the
	 *            array MUST contain the right number and type of elements
	 */
	public ValueObject(final Field[] fields, final Object[] fieldValues) {
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
			throw new RuntimeException("Vo is being instantiated with " + fields.length + " fields but "
					+ fieldValues.length + " values.");
		}
		this.fieldValues = fieldValues;
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
	protected Object[] getRawData() {
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
		logger.error("Invalid index {} used for setting value in a VO with {} values", idx, this.fields.length);
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeFields(final JsonWriter writer) throws IOException {
		JsonUtil.writeFields(this.fields, this.fieldValues, writer);
	}

	/**
	 * set parameter values to a prepared statement that uses this Vo as input
	 * source.
	 *
	 * @param ps
	 * @throws SQLException
	 */
	public void setPsParams(final PreparedStatement ps) throws SQLException {
		int idx = 0;
		for (final Field field : this.fields) {
			final Object value = this.fieldValues[idx];
			idx++;
			field.getValueType().setPsParam(ps, idx, value);
		}
	}

	/**
	 * read values from a result set for which this VO is designed as output
	 * data structure
	 *
	 * @param rs
	 * @throws SQLException
	 */
	public void readFromRs(final ResultSet rs) throws SQLException {
		int idx = 0;
		for (final Field field : this.fields) {
			this.fieldValues[idx] = field.getValueType().getFromRs(rs, idx + 1);
			idx++;
		}
	}

	/**
	 * make a copy of this Vo
	 *
	 * @return a copy of this that can be mutilated without affecting this
	 */
	public ValueObject copy() {
		final Object[] arr = Arrays.copyOf(this.fieldValues, this.fieldValues.length);
		return new ValueObject(this.fields, arr);
	}
}
