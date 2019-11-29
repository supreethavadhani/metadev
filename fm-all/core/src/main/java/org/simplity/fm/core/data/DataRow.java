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
 * data from (or for ) a db schema. Must be instantiated from a Schema/Form
 * instance
 *
 * @author simplity.org
 *
 */
public class DataRow {
	protected static final Logger logger = LoggerFactory.getLogger(DataRow.class);
	/**
	 * schema for which this data is created
	 */
	protected final Schema schema;
	/**
	 * data for (or from) the schema. Each element is the value of the
	 * corresponding field in the schema
	 */
	protected final Object[] dataRow;

	/**
	 *
	 * @param schema
	 *            non-null
	 */
	protected DataRow(final Schema schema) {
		this.schema = schema;
		this.dataRow = new Object[this.schema.getNbrFields()];
	}

	/**
	 *
	 * @param schema
	 *            non-null
	 * @param row
	 *            non-null. note that the number and type of elements must be as
	 *            per the schema, failing which a runtime exception is thrown
	 */
	protected DataRow(final Schema schema, final Object[] row) {
		this.schema = schema;
		final int nbrFields = schema.getNbrFields();
		if (nbrFields == row.length) {
			this.dataRow = row;
		} else {
			throw new RuntimeException("DbData " + this.schema.name + " has " + nbrFields + " fields but a row with "
					+ row.length + " values is being suppled as data row");
		}
	}

	/**
	 * @return field values
	 */
	public Object[] getRawData() {
		return this.dataRow;
	}

	/**
	 * @return the db schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 *
	 * @param fieldName
	 * @return Field in this form. null if no such field
	 */
	public int getFieldIndex(final String fieldName) {
		final Field field = this.schema.getField(fieldName);
		if (field != null) {
			return field.getIndex();
		}
		return -1;
	}

	/**
	 * @param idx
	 * @return object at the index. null if the index is out of range, or the
	 *         value at the index is null
	 */
	public Object getObject(final int idx) {
		if (this.idxOk(idx)) {
			return this.dataRow[idx];
		}
		return null;
	}

	private boolean idxOk(final int idx) {
		return idx >= 0 && idx < this.dataRow.length;
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
			this.dataRow[idx] = value;
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param idx
	 * @return get value at this index as long. 0 if the index is not valid, or
	 *         the value is not long
	 */
	public long getLongValue(final int idx) {
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
	public boolean setLongValue(final int idx, final long value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		final Field field = this.schema.getField(idx);

		final ValueType vt = field.getValueType();

		if (vt == ValueType.Integer) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.Decimal) {
			final double d = value;
			this.dataRow[idx] = d;
			return true;
		}

		if (vt == ValueType.Text) {

			this.dataRow[idx] = "" + value;
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
	public String getStringValue(final int idx) {
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
	public boolean setStringValue(final int idx, final String value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

		if (vt == ValueType.Text) {
			this.dataRow[idx] = value;
			return true;
		}

		final Object obj = vt.parse(value);

		if (obj != null) {
			this.dataRow[idx] = obj;
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
	public LocalDate getDateValue(final int idx) {
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
	public boolean setDateValue(final int idx, final LocalDate value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

		if (vt == ValueType.Date) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.dataRow[idx] = value.toString();
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
	public boolean getBoolValue(final int idx) {
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
	public boolean setBoolValue(final int idx, final boolean value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

		if (vt == ValueType.Boolean) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.dataRow[idx] = "" + value;
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
	public double getDecimalValue(final int idx) {
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
	public boolean setDecimlValue(final int idx, final double value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();
		if (vt == ValueType.Decimal) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.Integer) {
			this.dataRow[idx] = ((Number) value).longValue();
			return true;
		}

		if (vt == ValueType.Text) {
			this.dataRow[idx] = "" + value;
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
	public Instant getTimestampValue(final int idx) {
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
	public boolean setTimestampValue(final int idx, final Instant value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

		if (vt == ValueType.Timestamp) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.Text) {
			this.dataRow[idx] = value.toString();
			return true;
		}

		return false;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeAsJson(final Writer writer) throws IOException {
		try (JsonWriter jw = new JsonWriter(writer)) {
			jw.beginObject();
			this.schema.serializeToJson(this.dataRow, jw);
			jw.endObject();
		}
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeFields(final JsonWriter writer) throws IOException {
		this.schema.serializeToJson(this.dataRow, writer);
	}

	/*
	 * ************ DB Operations ************
	 */
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
		return asst.insert(handle, this.dataRow);
	}

	private void noOps() {
		logger.error("Form {} is not designed for db operation", this.schema.name);
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
		return asst.update(handle, this.dataRow);
	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean deleteFromDb(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.deleteFromDb(handle, this.dataRow);
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
	public boolean fetch(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.fetch(handle, this.dataRow);
	}

	/**
	 * fetch data for this form from a db using a filter condition, that is
	 * likely to get only one row. In any case,this method stops at first row.
	 *
	 * @param handle
	 * @param sql
	 *            sql that is likely to result in only one row.
	 *
	 * @return true if it is one row read.false if no data found for this form
	 *         (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean fetchFirstRow(final DbHandle handle, final FilterSql sql) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.fetchFirstRow(handle, sql.getSql(), sql.getWhereParams(), this.dataRow);
	}
}