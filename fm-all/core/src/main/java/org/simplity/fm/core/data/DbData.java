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
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * data from (or for ) a db schema
 *
 * @author simplity.org
 *
 */
public class DbData {
	protected final Logger logger = LoggerFactory.getLogger(DbData.class);
	/**
	 * schema for which this data is created
	 */
	protected final DbSchema schema;
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
	public DbData(final DbSchema schema) {
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
	public DbData(final DbSchema schema, final Object[] row) {
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
	public Object[] getDataRow() {
		return this.dataRow;
	}

	/**
	 * @return the db schema
	 */
	public DbSchema getSchema() {
		return this.schema;
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
	 * @return get value at this index as long. 0 if the indexis not valid, or
	 *         the value is not long
	 */
	public long getLongValue(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj != null && obj instanceof Number) {
			return ((Number) obj).longValue();
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

		if (vt == ValueType.INTEGER) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.DECIMAL) {
			final double d = value;
			this.dataRow[idx] = d;
			return true;
		}

		if (vt == ValueType.TEXT) {

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

		if (vt == ValueType.TEXT) {
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
		if (obj != null && obj instanceof LocalDate) {
			return (LocalDate) obj;
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

		if (vt == ValueType.DATE) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.TEXT) {
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
		final Object obj = this.getObject(idx);
		if (obj == null) {
			return false;
		}
		if (obj instanceof Boolean) {
			return (Boolean) obj;
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

		if (vt == ValueType.BOOLEAN) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.TEXT) {
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
		if (vt == ValueType.DECIMAL) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.INTEGER) {
			this.dataRow[idx] = ((Number) value).longValue();
			return true;
		}

		if (vt == ValueType.TEXT) {
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
	public Instant getTimestamp(final int idx) {
		final Object obj = this.getObject(idx);
		if (obj != null && obj instanceof Instant) {
			return (Instant) obj;
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
	public boolean setTimestamp(final int idx, final Instant value) {
		if (!this.idxOk(idx)) {
			return false;
		}

		final ValueType vt = this.schema.getField(idx).getValueType();

		if (vt == ValueType.TIMESTAMP) {
			this.dataRow[idx] = value;
			return true;
		}

		if (vt == ValueType.TEXT) {
			this.dataRow[idx] = value.toString();
			return true;
		}

		return false;
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param json
	 *            non-null
	 * @param ctx
	 *            can be null, if validations error need not be recorded into
	 *            the context. if non-null any validation error is added to it
	 */
	public void loadKeys(final JsonObject json, final IServiceContext ctx) {
		final int[] indexes = this.schema.getKeyIndexes();
		if (indexes == null) {
			return;
		}
		final Field[] fields = this.schema.getFields();
		for (final int idx : indexes) {
			final Field f = fields[idx];
			final String value = getTextAttribute(json, f.getName());
			validateAndSet(f, value, this.dataRow, idx, false, ctx, null, 0);
		}
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param inputValues
	 *            non-null collection of field values
	 * @param ctx
	 *            non-null to which any validation errors are added
	 */
	public void loadKeys(final Map<String, String> inputValues, final IServiceContext ctx) {
		final int[] indexes = this.schema.getKeyIndexes();
		if (indexes == null) {
			return;
		}

		final Field[] fields = this.schema.getFields();
		for (final int idx : indexes) {
			final Field f = fields[idx];
			final String value = inputValues.get(f.getName());
			validateAndSet(f, value, this.dataRow, idx, false, ctx, null, 0);
		}
	}

	private static String getTextAttribute(final JsonObject json, final String fieldName) {
		final JsonElement node = json.get(fieldName);
		if (node == null) {
			return null;
		}
		if (node.isJsonPrimitive()) {
			return node.getAsString();
		}
		return null;
	}

	private static void validateAndSet(final Field field, final String value, final Object[] row, final int idx,
			final boolean forInsert, final IServiceContext ctx, final String tableName, final int rowNbr) {
		if (value == null || value.isEmpty()) {
			row[idx] = null;
			field.validateNull(forInsert, ctx, tableName, rowNbr);
			return;
		}
		row[idx] = field.parse(value, ctx, tableName, rowNbr);
	}

	public void parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx, final String tableName,
			final int rowNbr) {
		this.setFeilds(json, forInsert, ctx, tableName, rowNbr);

		this.validateForm(ctx);
	}

	private void setFeilds(final JsonObject json, final boolean forInsert, final IServiceContext ctx,
			final String childName, final int rowNbr) {

		for (final Field field : this.schema.getFields()) {
			final int idx = field.getIndex();

			final String fieldName = field.getName();
			/*
			 * do we have to set value from our side?
			 */
			if (field.isTenantKey()) {
				this.dataRow[idx] = ctx.getTenantId();
				this.logger.info("tenant id set to field {}", fieldName);
				continue;
			}

			if (field.isUserId()) {
				this.dataRow[idx] = ctx.getUser().getUserId();
				this.logger.info("Field {} is user field, and is assigned value from the context", fieldName);
				continue;
			}

			final String value = getTextAttribute(json, fieldName);
			validateAndSet(field, value, this.dataRow, field.getIndex(), forInsert, ctx, childName, rowNbr);
		}
	}

	private void validateForm(final IServiceContext ctx) {
		final IValidation[] validations = this.schema.getValidations();
		final List<Message> errors = new ArrayList<>();
		if (validations != null) {
			for (final IValidation vln : validations) {
				final boolean ok = vln.isValid(this, errors);
				if (!ok) {
					this.logger.error("field {} failed an inter-field validaiton associated with it",
							vln.getFieldName());
				}
			}
		}
		if (errors.size() > 0) {
			ctx.addMessages(errors);
		}
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeAsJson(final Writer writer) throws IOException {
		try (JsonWriter gen = new JsonWriter(writer)) {
			gen.beginObject();
			this.writeFields(gen);
			gen.endObject();
		}
	}

	private void writeFields(final JsonWriter gen) throws IOException {
		for (final Field field : this.schema.getFields()) {
			writeField(gen, this.dataRow[field.getIndex()], field.getValueType());
		}
	}

	private static void writeField(final JsonWriter writer, final Object value, final ValueType vt) throws IOException {
		if (value == null) {
			writer.nullValue();
			return;
		}
		if (vt == ValueType.INTEGER || vt == ValueType.DECIMAL) {
			writer.value((Number) (value));
			return;
		}
		if (vt == ValueType.BOOLEAN) {
			writer.value((boolean) (value));
			return;
		}
		writer.value(value.toString());
	}
}
