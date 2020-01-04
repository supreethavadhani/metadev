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
import java.io.Writer;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.DbHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * <p>
 * Represents field values in an entity. Like an EntityBean/DTO/DAO all rolled
 * into one. This is one of the core classes
 * </p>
 * <p>
 * An entity consists of fields that carry values. We have chosen an array of
 * objects as the data-structure for field values. Of course, the fields are not
 * in any order, but we have have chosen an array over map for ease of access
 * with a generated meta-data for fields that include their index in the array
 * </p>
 *
 * <p>
 * While such an approach is quite useful for the framework to carry out its job
 * of auto-plumbing data between the client and the DB,it is quite painful for a
 * programmer to write custom code around such an API that requires array index.
 * For example setLongValue(int, long) is error prone, even if we provide static
 * literals for the index (like Customer.ID).Hence
 * Hence we also provide the standard getters and setters. These are generated
 * from the schema/form
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class SchemaData {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaData.class);
	/**
	 * schema for which this data is created
	 */
	protected final Schema schema;
	/**
	 * data for (or from) the schema. Each element is the value of the
	 * corresponding field in the schema
	 */
	protected Object[] fieldValues;

	protected SchemaData(final Schema schema, final Object[] values) {
		this.schema = schema;
		final int nbrFields = schema.getNbrFields();
		if (values == null) {
			this.fieldValues = new Object[nbrFields];
		} else {
			this.fieldValues = values;
		}
	}

	/**
	 * @return the db schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 *
	 * @param idx
	 *            index of the field. refer to getFieldIndex to get the index by
	 *            name
	 * @param value
	 *            value of the right type.
	 * @return true if value was indeed set. false if the field is not defined,
	 *         or
	 *         the type of object was not right for the field
	 */
	public boolean setObject(final int idx, final Object value) {
		if (this.idxOk(idx)) {
			this.fieldValues[idx] = value;
			return true;
		}
		return false;
	}

	/**
	 * @param idx
	 * @return object at the index. null if the index is out of range, or the
	 *         value at the index is null
	 */
	public Object getObject(final int idx) {
		if (this.idxOk(idx)) {
			return this.fieldValues[idx];
		}
		return null;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeAsJson(final Writer writer) throws IOException {
		try (JsonWriter jw = new JsonWriter(writer)) {
			jw.beginObject();
			this.schema.serializeToJson(this.fieldValues, jw);
			jw.endObject();
		}
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeFields(final JsonWriter writer) throws IOException {
		this.schema.serializeToJson(this.fieldValues, writer);
	}

	/**
	 * insert/create this form data into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	public boolean insert(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.insert(handle, this.fieldValues);
	}

	/**
	 * update this form data back into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.update(handle, this.fieldValues);
	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean delete(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.delete(handle, this.fieldValues);
	}

	/**
	 * fetch data for this form from a db
	 *
	 * @param handle
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean read(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.read(handle, this.fieldValues);
	}

	/**
	 * @param handle
	 * @param whereClauseStartingWithWhere
	 *            e.g. "WHERE a=? and b=?" null if all rows are to be read. Best
	 *            practice is to use parameters rather than dynamic sql. That is
	 *            you should use a=? rather than a = 32
	 * @param values
	 *            null or empty if where-clause is null or has no parameters.
	 *            every element MUST be non-null and must be one of the standard
	 *            objects we use String, Long, Double, Boolean, LocalDate,
	 *            Instant
	 * @return true if a row was read into this object. false otherwise
	 * @throws SQLException
	 */
	public boolean filterFirstOne(final DbHandle handle, final String whereClauseStartingWithWhere,
			final Object[] values) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		final Object[][] data = asst.filter(whereClauseStartingWithWhere, values, true, handle);
		if (data.length == 0) {
			return false;
		}
		this.fieldValues = data[0];
		return true;
	}

	/**
	 * insert or update this, based on the primary key. possible only if the
	 * primary key is generated
	 *
	 * @param handle
	 * @return true if it was indeed saved
	 * @throws SQLException
	 */
	public boolean save(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.save(handle, this.fieldValues);
	}

	/**
	 * @return field values
	 */
	protected Object[] getRawData() {
		return this.fieldValues;
	}

	private boolean idxOk(final int idx) {
		return idx >= 0 && idx < this.fieldValues.length;
	}

	/**
	 *
	 * @param idx
	 * @return get value at this index as long. 0 if the index is not valid, or
	 *         the value is not long
	 */
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
		if (!this.idxOk(idx)) {
			return false;
		}
		final Field field = this.schema.getField(idx);

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
		final Object obj = this.getObject(idx);
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
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

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
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

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
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

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
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();
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
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

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

	private void noOps() {
		logger.error("Form {} is not designed for db operation", this.schema.name);
	}

}