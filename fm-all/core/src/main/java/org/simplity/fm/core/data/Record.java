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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.serialize.IInputArray;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * meta-data provided for the fields.
 * </p>
 * Programmers may hand-code extended classes on a need basis
 *
 * NOTE: methods use fetch/assign instead of familiar get/set. This is to allow
 * generated extended class to use getters/setters for all their fields
 *
 * @author simplity.org
 *
 *
 *
 */
public class Record {
	private static final Logger logger = LoggerFactory.getLogger(Record.class);

	private final RecordMetaData metaData;

	/**
	 * current values
	 *
	 */
	protected final Object[] fieldValues;

	/**
	 * simplest way to create a record for local use: with no unique name or
	 * validations
	 *
	 * @param fields
	 *            non-null non-empty
	 * @param values
	 *            can be null
	 */
	public Record(final Field[] fields, final Object[] values) {
		this.metaData = new RecordMetaData(fields);
		this.fieldValues = values == null ? this.metaData.getDefaultValues() : values;
	}

	/**
	 * construct this record with a set of fields and values
	 */
	protected Record(final RecordMetaData recordMeta, final Object[] values) {
		this.metaData = recordMeta;
		this.fieldValues = values == null ? recordMeta.getDefaultValues() : values;
	}

	/**
	 * fetch used to avoid getters clashing with this method name
	 *
	 * @return unique name assigned to this record. concrete classes are
	 *         generally generated from meta data files, and this name is based
	 *         on the conventions used in the app
	 */
	public String fetchName() {
		return this.metaData.getName();
	}

	/**
	 * fetch used to avoid getters clashing with this method name
	 *
	 * @return the validations
	 */
	public IValidation[] fetchValidaitons() {
		return this.metaData.getValidations();
	}

	/**
	 * @return number of columns in this table
	 */
	public int length() {
		return this.metaData.getValidations().length;
	}

	/**
	 * @return the fields
	 */
	public Field[] fetchFields() {
		return this.metaData.getFields();
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
	public void assignValue(final int idx, final Object value) {
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
	public Object fetchValue(final int idx) {
		try {
			return this.fieldValues[idx];
		} catch (final Exception e) {
			this.logError(idx);
			return null;
		}
	}

	/**
	 * @return the underlying array of data. Returned array is not a copy, and
	 *         hence any changes made to that will affect this record
	 */
	public Object[] fetchRawData() {
		return this.fieldValues;
	}

	/**
	 *
	 * @param idx
	 * @return get value at this index as long. 0 if the index is not valid, or
	 *         the value is not long
	 */
	protected long fetchLongValue(final int idx) {
		final Object obj = this.fetchValue(idx);
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
	protected boolean assignLongValue(final int idx, final long value) {
		final Field field = this.metaData.getField(idx);
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
	protected String fetchStringValue(final int idx) {
		final Object obj = this.fetchValue(idx);
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
	protected boolean assignStringValue(final int idx, final String value) {
		final Field field = this.metaData.getField(idx);
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
	protected LocalDate fetchDateValue(final int idx) {
		final Object obj = this.fetchValue(idx);
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
	protected boolean assignDateValue(final int idx, final LocalDate value) {
		final Field field = this.metaData.getField(idx);
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
	protected boolean fetchBoolValue(final int idx) {
		Object obj = this.fetchValue(idx);
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
	protected boolean assignBoolValue(final int idx, final boolean value) {
		final Field field = this.metaData.getField(idx);
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
	protected double fetchDecimalValue(final int idx) {
		final Object obj = this.fetchValue(idx);

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
	protected boolean assignDecimlValue(final int idx, final double value) {
		final Field field = this.metaData.getField(idx);
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
	protected Instant fetchTimestampValue(final int idx) {
		final Object obj = this.fetchValue(idx);
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
	protected boolean assignTimestampValue(final int idx, final Instant value) {
		final Field field = this.metaData.getField(idx);
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
		logger.error("Invalid index {} used for setting value in a record with {} values", idx,
				this.metaData.getFields().length);
	}

	/**
	 * parse this record from a serialized input when teh object is the root.
	 *
	 * @param inputObject
	 *            input data
	 * @param forInsert
	 *            true if the data is being parsed for an insert operation,
	 *            false if it is meant for an update instead
	 * @param ctx
	 * @return true if all ok. false if any error message is added to the
	 *         context
	 */
	public boolean parse(final IInputObject inputObject, final boolean forInsert, final IServiceContext ctx) {
		return this.parse(inputObject, forInsert, ctx, null, 0);
	}

	/**
	 * parse this record from a serialized input when the record is inside an
	 * array as a child of a parent object
	 *
	 * @param inputObject
	 *            input data
	 * @param forInsert
	 *            true if the data is being parsed for an insert operation,
	 *            false if it is meant for an update instead
	 * @param ctx
	 * @param tableName
	 *            if the input data is for a table.collection if this record,
	 *            then this is the name of the attribute with which the table is
	 *            received. null if the data is at the root level, else n
	 * @param rowNbr
	 *            relevant if tablaeName is not-null.
	 * @return true if all ok. false if any error message is added to the
	 *         context
	 */
	public boolean parse(final IInputObject inputObject, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		boolean ok = true;
		for (final Field field : this.metaData.getFields()) {
			final String value = inputObject.getString(field.getName());
			if (!field.parseIntoRow(value, this.fieldValues, ctx, tableName, rowNbr)) {
				ok = false;
			}
		}

		final IValidation[] vals = this.metaData.getValidations();
		if (vals != null) {
			for (final IValidation vln : vals) {
				if (vln.isValid(this, ctx) == false) {
					logger.error("field {} failed an inter-field validaiton associated with it", vln.getFieldName());
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * parse this record from a serialized input
	 *
	 * @param inputObject
	 *            input object that has a member for this table
	 * @param memberName
	 *            name of the array-member in the object
	 * @param forInsert
	 *            true if the data is being parsed for an insert operation,
	 *            false if it is meant for an update instead
	 * @param ctx
	 * @return list of parsed data rows. null in case of any error.
	 */
	public List<? extends Record> parseTable(final IInputObject inputObject, final String memberName,
			final boolean forInsert, final IServiceContext ctx) {
		final List<Record> list = new ArrayList<>();
		final IInputArray arr = inputObject.getArray(memberName);
		if (arr == null) {
			logger.info("No data received for table named ", memberName);
			return list;
		}

		arr.forEach(ele -> {
			final Record rec = this.newInstance();
			if (rec.parse(ele, forInsert, ctx, memberName, 0)) {
				list.add(rec);
			} else {
				list.clear(); // indicate error condition
			}
			return true;
		});
		/*
		 * empty least means we encountered some error
		 */
		if (list.isEmpty()) {
			return null;
		}
		return list;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	protected void serializeRows(final ISerializer writer, final Object[][] rows) throws IOException {
		if (rows == null || rows.length == 0) {
			return;
		}
		for (final Object[] row : rows) {
			writer.beginObject();
			writer.fields(this.fetchFields(), row);
			writer.endObject();
		}
	}

	/**
	 * make a copy of this record.
	 *
	 * @return a copy of this that can be mutilated without affecting this
	 */
	public Record makeACopy() {
		return this.newInstance(Arrays.copyOf(this.fieldValues, this.fieldValues.length));
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
	 * set parameter values to a prepared statement that uses this Vo as input
	 * source.
	 *
	 * @param ps
	 * @throws SQLException
	 */
	public void setPsParams(final PreparedStatement ps) throws SQLException {
		int idx = 0;
		for (final Field field : this.fetchFields()) {
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
		for (final Field field : this.fetchFields()) {
			this.fieldValues[idx] = field.getValueType().getFromRs(rs, idx + 1);
			idx++;
		}
	}

	/**
	 * create a new instance of this object with this array of data. TO BE USED
	 * BY INTERNAL UTILITY.
	 *
	 * @param values
	 *
	 * @return a copy of this that can be mutilated without affecting this
	 */
	protected Record newInstance(final Object[] values) {
		return new Record(this.fetchFields(), values);
	}
}
