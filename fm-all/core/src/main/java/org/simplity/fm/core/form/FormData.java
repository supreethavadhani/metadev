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

package org.simplity.fm.core.form;

import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.InvalidValueException;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.http.LoggedInUser;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.IDbBatchWriter;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.rdb.IDbWriter;
import org.simplity.fm.core.service.IserviceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public class FormData {
	private static final Logger logger = LoggerFactory.getLogger(FormData.class);
	/**
	 * data structure describes the template for which this object provides
	 * actual data
	 */
	protected final Form form;
	/**
	 * field values. null if this template has no fields
	 */
	protected final Object[] fieldValues;
	/**
	 * data for child forms. null if this form has no children
	 */
	protected final FormData[][] childData;

	/**
	 * 
	 * @param form
	 * @param fieldValues
	 * @param childData
	 */
	public FormData(Form form, Object[] fieldValues, FormData[][] childData) {
		this.form = form;
		if (form.fields == null) {
			this.fieldValues = null;
		} else if (fieldValues == null) {
			this.fieldValues = new Object[form.fields.length];
		} else {
			this.fieldValues = fieldValues;
		}

		if (form.childForms == null) {
			this.childData = null;
		} else if (childData == null) {
			this.childData = new FormData[form.childForms.length][];
		} else {
			this.childData = childData;
		}
	}

	/**
	 * @param user
	 * @return is this user the owner of this form? If this form has no concept
	 *         of an owner, then this method returns true for any/all users.
	 */
	public boolean isOwner(LoggedInUser user) {
		int idx = this.form.userIdFieldIdx;
		if (idx == -1) {
			logger.warn("Form {} has not set user id field name. isOwner() will always return true",
					this.form.uniqueName);
			return true;
		}
		return user.getUserId().equals(this.fieldValues[idx]);
	}

	/**
	 * @param user
	 */
	public void setUserId(LoggedInUser user) {
		int idx = this.form.userIdFieldIdx;
		if (idx != -1) {
			this.fieldValues[idx] = user.getUserId();
		}
	}

	/**
	 * @return field values
	 */
	public Object[] getFieldValues() {
		return this.fieldValues;
	}

	/**
	 * 
	 * @return child data, or null if this form data has no child forms
	 */
	public FormData[][] getChildData() {
		return this.childData;
	}

	/**
	 * @return form for which data is carried
	 */
	public Form getForm() {
		return this.form;
	}

	/**
	 * @return get user id field, if one exists. null otherwise
	 */
	public String getUserId() {
		int idx = this.form.getUserIdFieldIdx();
		if (idx == -1) {
			return null;
		}
		Object obj = this.fieldValues[idx];
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}

	private boolean idxOk(int idx) {
		return idx >= 0 && idx < this.fieldValues.length;
	}

	/**
	 * 
	 * @param fieldName
	 * @return Field in this form. null if no such field
	 */
	public ValueType getValueType(String fieldName) {
		Field field = this.form.getField(fieldName);
		if (field == null) {
			return null;
		}
		return field.getValueType();
	}

	/**
	 * 
	 * @param fieldName
	 * @return Field in this form. null if no such field
	 */
	public int getFieldIndex(String fieldName) {
		Field field = this.form.getField(fieldName);
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
	public Object getObject(int idx) {
		if (this.idxOk(idx)) {
			return this.fieldValues[idx];
		}
		return null;
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
	public boolean setObject(int idx, Object value) {
		if (this.idxOk(idx)) {
			this.fieldValues[idx] = value;
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
	public long getLongValue(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setLongValue(int idx, long value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.INTEGER) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.DECIMAL) {
			double d = value;
			this.fieldValues[idx] = d;
			return true;
		}
		if (vt == ValueType.TEXT) {

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
	public String getStringValue(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setStringValue(int idx, String value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.TEXT) {
			this.fieldValues[idx] = value;
			return true;
		}
		Object obj = vt.parse(value);
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
	public LocalDate getDateValue(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setDateValue(int idx, LocalDate value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.DATE) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.TEXT) {
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
	public boolean getBoolValue(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setBoolValue(int idx, boolean value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.BOOLEAN) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.TEXT) {
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
	public double getDecimalValue(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setDecimlValue(int idx, double value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.DECIMAL) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.INTEGER) {
			this.fieldValues[idx] = ((Number) value).longValue();
			return true;
		}
		if (vt == ValueType.TEXT) {
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
	public Instant getTimestamp(int idx) {
		Object obj = this.getObject(idx);
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
	public boolean setTimestamp(int idx, Instant value) {
		if (!this.idxOk(idx)) {
			return false;
		}
		ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.TIMESTAMP) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.TEXT) {
			this.fieldValues[idx] = value.toString();
			return true;
		}
		return false;
	}

	/**
	 * parse and set value for a field.
	 * 
	 * @param idx
	 *            valid index for this form
	 * @param value
	 *            string value to be parsed for this field
	 * @param ctx
	 * 
	 */
	public void parseField(int idx, String value, IserviceContext ctx) {
		if (!this.idxOk(idx)) {
			ctx.addMessage(Message.newError(idx + " is not valid index for form " + this.form.getFormId()));
			return;
		}
		Field f = this.form.getFields()[idx];
		validateAndSet(f, value, this.fieldValues, idx, false, ctx, null, 0);
	}

	/**
	 * load from a JSON node with no validation. To be called when loading from
	 * a dependable source
	 * 
	 * @param json
	 */
	public void load(JsonObject json) {
		this.loadWorker(json, true, true, null, null, 0);
	}

	/**
	 * load keys from a JSON. input is suspect.
	 * 
	 * @param json
	 *            non-null
	 * @param ctx
	 *            non-null to which any validation errors are added
	 */
	public void loadKeys(JsonObject json, IserviceContext ctx) {
		int[] indexes = this.form.getKeyIndexes();
		if (indexes == null) {
			return;
		}
		Field[] fields = this.form.getFields();
		int userIdx = this.form.getUserIdFieldIdx();
		for (int idx : indexes) {
			if (idx == userIdx) {
				this.setUserId(ctx.getUser());
			} else {
				Field f = fields[idx];
				String value = getTextAttribute(json, f.getFieldName());
				validateAndSet(f, value, this.fieldValues, idx, false, ctx, null, 0);
			}
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
	public void loadKeys(Map<String, String> inputValues, IserviceContext ctx) {
		int[] indexes = this.form.getKeyIndexes();
		if (indexes == null) {
			return;
		}
		
		Field[] fields = this.form.getFields();
		int userIdx = this.form.getUserIdFieldIdx();
		for (int idx : indexes) {
			if (idx == userIdx) {
				this.setUserId(ctx.getUser());
			} else {
				Field f = fields[idx];
				String value = inputValues.get(f.getFieldName());
				validateAndSet(f, value, this.fieldValues, idx, false, ctx, null, 0);
			}
		}
	}

	/**
	 * load keys from a JSON. input is suspect.
	 * 
	 * @param inputData
	 *            non-null collection of field values
	 * @return true if unique fields are extracted. false otherwise
	 */
	public boolean loadUniqKeys(JsonObject inputData) {
		int[] indexes = this.form.getUniqIndexes();
		if (indexes == null) {
			return false;
		}
		Field[] fields = this.form.getFields();
		for (int idx : indexes) {
			Field f = fields[idx];
			String value = null;
			JsonPrimitive ele = inputData.getAsJsonPrimitive(f.getFieldName());
			if (ele != null) {
				value = ele.getAsString();
			}
			boolean result = validateAndSet(f, value, this.fieldValues, idx, false, null, null, 0);
			if (!result) {
				return false;
			}
		}
		return true;
	}

	/**
	 * load from a JSON node that is not dependable. Like input from a client
	 * 
	 * @param json
	 *            non-null
	 * @param allFieldsAreOptional
	 *            true if this is for a draft-save operation, where we validate
	 *            only the fields that the user has opted to type. MUST be
	 *            called with true value for final submit operation
	 * @param forInsert
	 *            true if this data is meant to create a new row (insert
	 *            operation). In this case, primary key is skipped if it is to
	 *            be generated
	 * @param ctx
	 *            non-null
	 */
	public void validateAndLoad(JsonObject json, boolean allFieldsAreOptional, boolean forInsert, IserviceContext ctx) {
		this.loadWorker(json, allFieldsAreOptional, forInsert, ctx, null, 0);
	}

	private void loadWorker(JsonObject json, boolean allFieldsAreOptional, boolean forInsert, IserviceContext ctx,
			String childName, int rowNbr) {
		boolean keyIsOptional = false;
		if (forInsert) {
			keyIsOptional = this.form.getDbMetaData().generatedColumnName != null;
		}
		this.setFeilds(json, allFieldsAreOptional, keyIsOptional, ctx, childName, rowNbr);

		ChildForm[] children = this.form.getChildForms();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				this.childData[i] = this.validateChild(children[i], json, allFieldsAreOptional, keyIsOptional, ctx);
			}
		}
		if (!allFieldsAreOptional) {
			this.validateForm(ctx);
		}

	}

	private FormData[] validateChild(ChildForm childForm, JsonObject json, boolean allFieldsAreOptional,
			boolean forInsert, IserviceContext ctx) {
		String fieldName = childForm.fieldName;
		JsonElement childNode = json.get(fieldName);
		if (childNode == null) {
			if (childForm.minRows > 0) {
				ctx.addMessage(Message.newFieldError(fieldName, childForm.errorMessageId));
			}
			return null;
		}

		if (childForm.isTabular == false) {
			if (!childNode.isJsonObject()) {
				logger.error(
						"Form {} has a child form named {} and hence an object is expeted. But {} is received as data",
						this.form.getFormId(), fieldName, childNode.getClass().getSimpleName());
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}
			FormData fd = childForm.form.newFormData();
			fd.loadWorker((JsonObject) childNode, allFieldsAreOptional, forInsert, ctx, null, 0);
			FormData[] result = { fd };
			return result;
		}

		JsonArray arr = null;
		int n = 0;
		if (childNode.isJsonArray()) {
			arr = (JsonArray) childNode;
			n = arr.size();
			if (allFieldsAreOptional == false) {
				if ((n < childForm.minRows || n > childForm.maxRows)) {
					logger.error(
							"Form {} has a child form named {} and hence an object is expeted. But {} is received as data",
							this.form.getFormId(), fieldName, childNode.getClass().getSimpleName());
					arr = null;
				}
			}
		}

		if (arr == null) {
			ctx.addMessage(Message.newFieldError(fieldName, childForm.errorMessageId));
			return null;
		}

		if (n == 0) {
			return null;
		}
		List<FormData> fds = new ArrayList<>();
		for (int j = 0; j < n; j++) {
			JsonElement col = arr.get(j);

			if (col == null || !col.isJsonObject()) {
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				continue;
			}
			FormData fd = childForm.form.newFormData();
			fds.add(fd);
			fd.loadWorker((JsonObject) col, allFieldsAreOptional, forInsert, ctx, fieldName, j);
		}
		if (fds.size() == 0) {
			return null;
		}
		return fds.toArray(new FormData[0]);
	}

	private void setFeilds(JsonObject json, boolean allFieldsAreOptional, boolean keyIsOptional, IserviceContext ctx,
			String childName, int rowNbr) {

		int userIdx = this.form.getUserIdFieldIdx();
		for (Field field : this.form.getFields()) {
			int idx = field.getIndex();
			if (userIdx > -1 && field.getIndex() == userIdx) {
				this.setUserId(ctx.getUser());
				userIdx = -1; //we are done
				continue;
			}
			ColumnType ct = field.getColumnType();
			if (ct != null) {
				if (!ct.isInput()) {
					logger.info("Field {} skipped as we do not expect it from client", field.getFieldName());
					continue;
				}
				if (keyIsOptional && (ct == ColumnType.GeneratedPrimaryKey || ct == ColumnType.PrimaryKey)) {
					logger.info("key field {} is skipped as we do not expect it from client", field.getFieldName());
					continue;
				}
				if (ct == ColumnType.ModifiedBy || ct == ColumnType.CreatedBy) {
					this.fieldValues[idx] = ctx.getUser().getUserId();
				}
			}

			String value = getTextAttribute(json, field.getFieldName());
			validateAndSet(field, value, this.fieldValues, field.getIndex(), allFieldsAreOptional, ctx, childName,
					rowNbr);
		}
	}

	private static boolean validateAndSet(Field field, String value, Object[] row, int idx,
			boolean allFieldsAreOptional, IserviceContext ctx, String childName, int rowNbr) {
		if (value == null || value.isEmpty()) {
			if (allFieldsAreOptional) {
				row[idx] = null;
				return false;
			}
		}
		try {
			row[idx] = field.parse(value);
			return true;
		} catch (InvalidValueException e) {
			logger.error("{} is not a valid value for {} which is of data-type {} and value type {}", value,
					field.getFieldName(), field.getDataType().getName(), field.getDataType().getValueType());
			if (ctx != null) {
				ctx.addMessage(Message.newObjectFieldError(field.getFieldName(), childName, field.getMessageId(),
						rowNbr, e.getParams()));
			}
			return false;
		}
	}

	/**
	 * @param serviceContext
	 */
	private void validateForm(IserviceContext ctx) {
		IValidation[] validations = this.form.getValidations();
		List<Message> errors = new ArrayList<>();
		if (validations != null) {
			for (IValidation vln : validations) {
				boolean ok = vln.isValid(this, errors);
				if (!ok) {
					logger.error("field {} failed an inter-field validaiton associated with it", vln.getFieldName());
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
	public void serializeAsJson(Writer writer) throws IOException {
		try (JsonWriter gen = new JsonWriter(writer)) {
			this.serialize(gen);
		}
	}

	private void serialize(JsonWriter gen) throws IOException {
		gen.beginObject();
		writeFields(gen);
		if (this.childData != null) {
			this.serializeChildren(gen);
		}

		gen.endObject();
	}

	private void serializeChildren(JsonWriter gen) throws IOException {
		int i = 0;
		for (ChildForm cf : this.form.childForms) {
			FormData[] fd = this.childData[i];
			if (fd == null) {
				continue;
			}
			gen.name(cf.fieldName);
			if (cf.isTabular) {
				gen.beginArray();
				for (FormData cd : fd) {
					cd.serialize(gen);
				}
				gen.endArray();
			} else {
				fd[0].serialize(gen);
			}
			i++;
		}
	}

	private void writeFields(JsonWriter gen) throws IOException {
		for (Field field : this.form.getFields()) {
			Object value = this.fieldValues[field.getIndex()];
			if (value == null) {
				continue;
			}
			gen.name(field.getFieldName());
			writeField(gen, value, field.getValueType());
		}
	}

	private static void writeField(JsonWriter writer, Object value, ValueType vt) throws IOException {
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

	private static String getTextAttribute(JsonObject json, String fieldName) {
		JsonElement node = json.get(fieldName);
		if (node == null) {
			return null;
		}
		if (node.isJsonPrimitive()) {
			return node.getAsString();
		}
		return null;
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
	public boolean insert(DbHandle handle) throws SQLException {
		DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}
		int n = 0;
		Object[] values = this.fieldValues;
		if (meta.generatedColumnName != null) {
			try {
				final long[] generatedKeys = new long[1];
				n = handle.insertAndGenerteKeys(new IDbWriter() {

					@Override
					public String getPreparedStatement() {
						return meta.insertClause;
					}

					@Override
					public void setParams(PreparedStatement ps) throws SQLException {
						int posn = 1;
						for (FormDbParam p : meta.insertParams) {
							p.valueType.setPsParam(ps, posn, values[p.idx]);
							posn++;
						}
					}

				}, meta.generatedColumnName, generatedKeys);
			} catch (SQLException e) {
				String msg = toMessage(e, meta.insertClause, meta.insertParams, values);
				logger.error(msg);
				throw new SQLException(msg, e);
			}
		} else {
			n = writeWorker(handle, meta.insertClause, meta.insertParams, values);
		}
		if (n == 0) {
			return false;
		}
		if (meta.dbLinks != null) {
			this.insertChildren(handle);
		}
		return true;
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
	public boolean update(DbHandle handle) throws SQLException {
		DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}
		int nbr = writeWorker(handle, meta.updateClause, meta.updateParams, this.fieldValues);
		return nbr > 0;
	}

	/**
	 * remove this form data from the db
	 * 
	 * @param handle
	 * 
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean deleteFromDb(DbHandle handle) throws SQLException {
		DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}
		String sql = meta.deleteClause + meta.whereClause;
		int nbr = writeWorker(handle, sql, meta.whereParams, this.fieldValues);
		if (nbr > 0 && meta.dbLinks != null) {
			for (DbLink link : meta.dbLinks) {
				if (link != null) {
					DbMetaData cm = link.childMeta;
					/*
					 * delete childTable where link-child-fields =
					 * parent-link-field-values
					 */
					writeWorker(handle, cm.deleteClause + link.linkWhereClause, link.linkParentParams,
							this.fieldValues);
				}
			}
		}
		return nbr > 0;
	}

	private static int writeWorker(DbHandle handle, String sql, FormDbParam[] params, Object[] values)
			throws SQLException {
		try {
			int n = handle.write(new IDbWriter() {

				@Override
				public String getPreparedStatement() {
					return sql;
				}

				@Override
				public void setParams(PreparedStatement ps) throws SQLException {
					int posn = 1;
					for (FormDbParam p : params) {
						p.valueType.setPsParam(ps, posn, values[p.idx]);
						posn++;
					}
				}

			});
			return n;
		} catch (SQLException e) {
			String msg = toMessage(e, sql, params, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
	}

	private static String toMessage(SQLException e, String sql, FormDbParam[] params, Object[] values) {
		StringBuilder buf = new StringBuilder();
		buf.append("Sql Exception : ").append(e.getMessage());
		buf.append("SQL:").append(sql).append("\nParameters");
		SQLException e1 = e.getNextException();
		if (e1 != null) {
			buf.append("\nLinked to the SqlExcpetion: ").append(e1.getMessage());
		}
		int idx = 1;
		for (FormDbParam p : params) {
			buf.append('\n').append(idx).append(". type=").append(p.valueType);
			buf.append(" value=").append(values[p.idx]);
			idx++;
		}
		return buf.toString();
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
	public boolean fetch(DbHandle handle) throws SQLException {
		DbMetaData meta = this.form.dbMetaData;
		if (meta == null) {
			logger.error("{} is not designed for any db operation", this.form.getFormId());
			return false;
		}
		boolean ok = fetchWorker(handle, meta.selectClause + meta.whereClause, this.fieldValues, meta.whereParams,
				meta.selectParams);
		if (ok && meta.dbLinks != null) {
			this.fetchChildren(handle);
		}
		return ok;
	}

	/**
	 * @param handle
	 * @return true if read is successful. false otherwise
	 * @throws SQLException
	 */
	public boolean fetchUsingUniqueKeys(DbHandle handle) throws SQLException {
		DbMetaData meta = this.form.dbMetaData;
		if (meta == null) {
			logger.error("{} is not designed for any db operation", this.form.getFormId());
			return false;
		}
		if (meta.uniqueClause == null) {
			logger.error("{} has no unque keys defined. can not be fetched with unique keys.", this.form.getFormId());
			return false;
		}
		boolean ok = fetchWorker(handle, meta.selectClause + meta.uniqueClause, this.fieldValues, meta.uniqueParams,
				meta.selectParams);
		if (ok && meta.dbLinks != null) {
			this.fetchChildren(handle);
		}
		return ok;
	}

	private static boolean fetchWorker(DbHandle driver, String sql, Object[] values, FormDbParam[] setters,
			FormDbParam[] getters) throws SQLException {
		boolean[] result = new boolean[1];
		driver.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				if(setters == null ||setters.length == 0) {
					return;
				}
				int posn = 1;
				for (FormDbParam p : setters) {
					p.valueType.setPsParam(ps, posn, values[p.idx]);
					posn++;
				}
			}

			@Override
			public boolean readARow(ResultSet rs) throws SQLException {
				int posn = 1;
				for (FormDbParam p : getters) {
					values[p.idx] = p.valueType.getFromRs(rs, posn);
					posn++;
				}
				result[0] = true;
				return false;
			}
		});
		return result[0];
	}

	/**
	 * @param driver
	 * @throws SQLException
	 */
	private void fetchChildren(DbHandle handle) throws SQLException {
		int idx = 0;
		for (DbLink link : this.form.dbMetaData.dbLinks) {
			if (link != null) {
				Form childForm = this.form.childForms[idx].form;
				DbMetaData meta = link.childMeta;
				this.childData[idx] = fetchDataWorker(handle, childForm, meta.selectClause + link.linkWhereClause,
						this.fieldValues, link.linkParentParams, meta.selectParams);
			}
			idx++;
		}
	}

	/**
	 * @param driver
	 * @throws SQLException
	 */
	private void insertChildren(DbHandle handle) throws SQLException {
		int idx = -1;
		for (DbLink link : this.form.dbMetaData.dbLinks) {
			idx++;
			if (link == null) {
				continue;
			}

			FormData[] rows = this.childData[idx];
			if (rows == null || rows.length == 0) {
				continue;
			}

			DbMetaData meta = link.childMeta;
			Object[] values = this.fieldValues;
			handle.write(new IDbBatchWriter() {
				int rowIdx = 0;

				@Override
				public String getPreparedStatement() {
					return meta.insertClause;
				}

				@Override
				public boolean setParams(PreparedStatement ps) throws SQLException {
					FormData fd = rows[this.rowIdx];
					int posn = 0;
					/*
					 * copy parent keys to the child row
					 */
					for (String nam : link.childLinkNames) {
						fd.fieldValues[fd.getFieldIndex(nam)] = values[link.linkParentParams[posn].idx];
						posn++;
					}
					posn = 1;
					for (FormDbParam p : meta.insertParams) {
						p.valueType.setPsParam(ps, posn, fd.fieldValues[p.idx]);
						posn++;
					}

					this.rowIdx++;
					return (this.rowIdx < rows.length);
				}
			});
		}
	}

	static FormData[] fetchDataWorker(DbHandle handle, Form form, String sql, Object[] values, FormDbParam[] setters,
			FormDbParam[] getters) throws SQLException {
		List<FormData> result = new ArrayList<>();
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				int posn = 1;
				for (FormDbParam p : setters) {
					p.valueType.setPsParam(ps, posn, values[p.idx]);
					posn++;
				}
			}

			@Override
			public boolean readARow(ResultSet rs) throws SQLException {
				FormData fd = form.newFormData();
				result.add(fd);
				int posn = 1;
				for (FormDbParam p : getters) {
					fd.fieldValues[p.idx] = p.valueType.getFromRs(rs, posn);
					posn++;
				}
				return true;
			}
		});
		return result.toArray(new FormData[0]);
	}
}
