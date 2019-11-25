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
import org.simplity.fm.core.rdb.IDbMultipleWriter;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.rdb.IDbWriter;
import org.simplity.fm.core.service.IServiceContext;
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
	protected static final Logger logger = LoggerFactory.getLogger(FormData.class);
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
	public FormData(final Form form, final Object[] fieldValues, final FormData[][] childData) {
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
	public boolean isOwner(final LoggedInUser user) {
		final int idx = this.form.userIdFieldIdx;
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
	public void setUserId(final LoggedInUser user) {
		final int idx = this.form.userIdFieldIdx;
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
	 * @param childIdx
	 * @return child data, or null if this form data has no child forms, or the
	 *         idx is out of range
	 */
	public FormData[] getChildData(final int childIdx) {
		if (this.childData == null) {
			return null;
		}
		if (childIdx < this.childData.length) {
			return this.childData[childIdx];
		}
		return null;
	}

	/**
	 * to be used with great care, to ensure that the FD is appropriate for this
	 * child
	 *
	 * @param childIdx
	 * @param data
	 *            must be a form data for the right form
	 */
	public void setChildData(final int childIdx, final FormData[] data) {
		if (this.childData == null || childIdx >= this.childData.length) {
			logger.error("Invalid child index {} ", childIdx);
			return;
		}
		/*
		 * validate the child form name if possible..
		 */
		if (data != null && data.length > 0) {
			final String id = data[0].form.uniqueName;
			final String otherId = this.form.childForms[childIdx].form.uniqueName;
			if (id.equals(otherId) == false) {
				logger.error(
						"{} is the child form of this form, but a formData for form {} is being assigned as child data. Request rejected");
				return;
			}
		}
		this.childData[childIdx] = data;
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
		final int idx = this.form.getUserIdFieldIdx();
		if (idx == -1) {
			return null;
		}
		final Object obj = this.fieldValues[idx];
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}

	private boolean idxOk(final int idx) {
		return idx >= 0 && idx < this.fieldValues.length;
	}

	/**
	 *
	 * @param fieldName
	 * @return Field in this form. null if no such field
	 */
	public ValueType getValueType(final String fieldName) {
		final Field field = this.form.getField(fieldName);
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
	public int getFieldIndex(final String fieldName) {
		final Field field = this.form.getField(fieldName);
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
	public boolean setObject(final int idx, final Object value) {
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.INTEGER) {
			this.fieldValues[idx] = value;
			return true;
		}
		if (vt == ValueType.DECIMAL) {
			final double d = value;
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
		if (vt == ValueType.TEXT) {
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
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
		final ValueType vt = this.form.getFields()[idx].getValueType();
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
	public void parseField(final int idx, final String value, final IServiceContext ctx) {
		if (!this.idxOk(idx)) {
			ctx.addMessage(Message.newError(idx + " is not valid index for form " + this.form.getFormId()));
			return;
		}
		final Field f = this.form.getFields()[idx];
		validateAndSet(f, value, this.fieldValues, idx, false, ctx, null, 0);
	}

	/**
	 * load from a JSON node with no validation. To be called when loading from
	 * a dependable source
	 *
	 * @param json
	 */
	public void load(final JsonObject json) {
		this.loadWorker(json, true, true, null, null, 0);
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param json
	 *            non-null
	 * @param ctx
	 *            can be null, if validations error need not be recorded into
	 *            the context. if non-null any validation error is added to it
	 * @return true of all keys are successfully loaded. false if any error is
	 *         encountered. Also false if this form has not defined any primary
	 *         key
	 */
	public boolean loadKeys(final JsonObject json, final IServiceContext ctx) {
		final int[] indexes = this.form.getKeyIndexes();
		if (indexes == null) {
			return false;
		}
		final Field[] fields = this.form.getFields();
		final int userIdx = this.form.getUserIdFieldIdx();
		boolean allOk = true;
		for (final int idx : indexes) {
			if (idx == userIdx && ctx != null) {
				this.setUserId(ctx.getUser());
			} else {
				final Field f = fields[idx];
				final String value = getTextAttribute(json, f.getFieldName());
				final boolean ok = validateAndSet(f, value, this.fieldValues, idx, false, ctx, null, 0);
				allOk = allOk && ok;
			}
		}
		return allOk;
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
		final int[] indexes = this.form.getKeyIndexes();
		if (indexes == null) {
			return;
		}

		final Field[] fields = this.form.getFields();
		final int userIdx = this.form.getUserIdFieldIdx();
		for (final int idx : indexes) {
			if (idx == userIdx) {
				this.setUserId(ctx.getUser());
			} else {
				final Field f = fields[idx];
				final String value = inputValues.get(f.getFieldName());
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
	public boolean loadUniqKeys(final JsonObject inputData) {
		final int[] indexes = this.form.getUniqIndexes();
		if (indexes == null) {
			return false;
		}
		final Field[] fields = this.form.getFields();
		for (final int idx : indexes) {
			final Field f = fields[idx];
			String value = null;
			final JsonPrimitive ele = inputData.getAsJsonPrimitive(f.getFieldName());
			if (ele != null) {
				value = ele.getAsString();
			}
			final boolean result = validateAndSet(f, value, this.fieldValues, idx, false, null, null, 0);
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
	public void validateAndLoad(final JsonObject json, final boolean allFieldsAreOptional, final boolean forInsert,
			final IServiceContext ctx) {
		this.loadWorker(json, allFieldsAreOptional, forInsert, ctx, null, 0);
	}

	private void loadWorker(final JsonObject json, final boolean allFieldsAreOptional, final boolean forInsert,
			final IServiceContext ctx, final String childName, final int rowNbr) {
		boolean keyIsOptional = false;
		if (forInsert) {
			final DbMetaData meta = this.form.getDbMetaData();
			if (meta != null) {
				keyIsOptional = meta.generatedColumnName != null;
			}
		}
		this.setFeilds(json, allFieldsAreOptional, keyIsOptional, ctx, childName, rowNbr);

		final ChildForm[] children = this.form.getChildForms();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				this.childData[i] = this.validateChild(children[i], json, allFieldsAreOptional, keyIsOptional, ctx);
			}
		}
		if (!allFieldsAreOptional) {
			this.validateForm(ctx);
		}
	}

	private FormData[] validateChild(final ChildForm childForm, final JsonObject json,
			final boolean allFieldsAreOptional, final boolean forInsert, final IServiceContext ctx) {
		final String fieldName = childForm.fieldName;
		final JsonElement childNode = json.get(fieldName);
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
			final FormData fd = childForm.form.newFormData();
			fd.loadWorker((JsonObject) childNode, allFieldsAreOptional, forInsert, ctx, null, 0);
			final FormData[] result = { fd };
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
		final List<FormData> fds = new ArrayList<>();
		for (int j = 0; j < n; j++) {
			final JsonElement col = arr.get(j);

			if (col == null || !col.isJsonObject()) {
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				continue;
			}
			final FormData fd = childForm.form.newFormData();
			fds.add(fd);
			fd.loadWorker((JsonObject) col, allFieldsAreOptional, forInsert, ctx, fieldName, j);
		}
		if (fds.size() == 0) {
			return null;
		}
		return fds.toArray(new FormData[0]);
	}

	private void setFeilds(final JsonObject json, final boolean allFieldsAreOptional, final boolean keyIsOptional,
			final IServiceContext ctx, final String childName, final int rowNbr) {

		boolean fieldIsOptional = allFieldsAreOptional;
		int userIdx = this.form.getUserIdFieldIdx();
		for (final Field field : this.form.getFields()) {
			final int idx = field.getIndex();

			if (userIdx > -1 && field.getIndex() == userIdx) {
				this.setUserId(ctx.getUser());
				userIdx = -1; // we are done
				continue;
			}

			final String fieldName = field.getFieldName();
			final ColumnType ct = field.getColumnType();
			if (ct != null) {
				/*
				 * do we have to set value from our side?
				 */
				if (ct == ColumnType.TenantKey) {
					this.fieldValues[idx] = ctx.getTenantId();
					logger.info("tenant id set to field {}", fieldName);
					continue;
				}

				if (ct == ColumnType.ModifiedBy || ct == ColumnType.CreatedBy) {
					this.fieldValues[idx] = ctx.getUser().getUserId();
					logger.info("Field {} is user field, and is assigned value from the context", fieldName);
					continue;
				}

				/*
				 * should we force this to be optional?
				 */
				if (ct == ColumnType.GeneratedPrimaryKey || ct == ColumnType.PrimaryAndParentKey
						|| ct == ColumnType.PrimaryKey) {
					fieldIsOptional = keyIsOptional;
				} else if (ct.isInput() == false) {
					fieldIsOptional = true;
				}
			}

			final String value = getTextAttribute(json, field.getFieldName());
			validateAndSet(field, value, this.fieldValues, field.getIndex(), fieldIsOptional, ctx, childName, rowNbr);
		}
	}

	private static boolean validateAndSet(final Field field, final String value, final Object[] row, final int idx,
			final boolean allFieldsAreOptional, final IServiceContext ctx, final String childName, final int rowNbr) {
		if (value == null || value.isEmpty()) {
			if (allFieldsAreOptional) {
				row[idx] = null;
				return false;
			}
		}
		try {
			row[idx] = field.parse(value);
			return true;
		} catch (final InvalidValueException e) {
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
	private void validateForm(final IServiceContext ctx) {
		final IValidation[] validations = this.form.getValidations();
		final List<Message> errors = new ArrayList<>();
		if (validations != null) {
			for (final IValidation vln : validations) {
				final boolean ok = vln.isValid(this, errors);
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
	public void serializeAsJson(final Writer writer) throws IOException {
		try (JsonWriter gen = new JsonWriter(writer)) {
			this.serialize(gen);
		}
	}

	private void serialize(final JsonWriter gen) throws IOException {
		gen.beginObject();
		this.writeFields(gen);
		if (this.childData != null) {
			this.serializeChildren(gen);
		}

		gen.endObject();
	}

	private void serializeChildren(final JsonWriter gen) throws IOException {
		int i = 0;
		for (final ChildForm cf : this.form.childForms) {
			final FormData[] fd = this.childData[i];
			if (fd == null) {
				continue;
			}
			gen.name(cf.fieldName);
			if (cf.isTabular) {
				gen.beginArray();
				for (final FormData cd : fd) {
					cd.serialize(gen);
				}
				gen.endArray();
			} else {
				fd[0].serialize(gen);
			}
			i++;
		}
	}

	private void writeFields(final JsonWriter gen) throws IOException {
		for (final Field field : this.form.getFields()) {
			final Object value = this.fieldValues[field.getIndex()];
			if (value == null) {
				continue;
			}
			gen.name(field.getFieldName());
			writeField(gen, value, field.getValueType());
		}
	}

	private static void writeField(final JsonWriter writer, final Object value, final ValueType vt) throws IOException {
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
		final DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}
		int n = 0;
		final Object[] values = this.fieldValues;
		if (meta.generatedColumnName != null) {
			try {
				final long[] generatedKeys = new long[1];
				n = handle.insertAndGenerteKeys(new IDbWriter() {

					@Override
					public String getPreparedStatement() {
						return meta.insertClause;
					}

					@Override
					public void setParams(final PreparedStatement ps) throws SQLException {
						int posn = 1;
						final StringBuilder sbf = new StringBuilder("Parameter Values");
						for (final FormDbParam p : meta.insertParams) {
							final Object value = values[p.idx];
							p.valueType.setPsParam(ps, posn, value);
							sbf.append('\n').append(posn).append('=').append(value);
							posn++;
						}
						logger.info(sbf.toString());
					}

				}, meta.generatedColumnName, generatedKeys);
				final long id = generatedKeys[0];
				if (id == 0) {
					logger.error("DB handler did not return generated key");
				} else {
					this.fieldValues[this.form.keyIndexes[0]] = generatedKeys[0];
					logger.info("Generated key {] assigned back to form data", id);
				}
			} catch (final SQLException e) {
				final String msg = toMessage(e, meta.insertClause, meta.insertParams, values);
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
	public boolean update(final DbHandle handle) throws SQLException {
		final DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}

		final int nbr = writeWorker(handle, meta.updateClause, meta.updateParams, this.fieldValues);
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
	public boolean deleteFromDb(final DbHandle handle) throws SQLException {
		final DbMetaData meta = this.form.getDbMetaData();
		if (meta == null) {
			logger.error("Form {} is not designed for db operation", this.form.getFormId());
			return false;
		}
		final String sql = meta.deleteClause + meta.whereClause;
		final int nbr = writeWorker(handle, sql, meta.whereParams, this.fieldValues);
		if (nbr > 0 && meta.dbLinks != null) {
			for (final DbLink link : meta.dbLinks) {
				if (link != null) {
					final DbMetaData cm = link.childMeta;
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

	private static int writeWorker(final DbHandle handle, final String sql, final FormDbParam[] params,
			final Object[] values) throws SQLException {
		try {
			final int n = handle.write(new IDbWriter() {

				@Override
				public String getPreparedStatement() {
					return sql;
				}

				@Override
				public void setParams(final PreparedStatement ps) throws SQLException {
					int posn = 1;
					final StringBuilder sbf = new StringBuilder("Parameter Values");
					for (final FormDbParam p : params) {
						final Object value = values[p.idx];
						p.valueType.setPsParam(ps, posn, value);
						sbf.append('\n').append(posn).append('=').append(value);
						posn++;
					}
					logger.info(sbf.toString());
				}

			});
			return n;
		} catch (final SQLException e) {
			final String msg = toMessage(e, sql, params, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
	}

	private static String toMessage(final SQLException e, final String sql, final FormDbParam[] params,
			final Object[] values) {
		final StringBuilder buf = new StringBuilder();
		buf.append("Sql Exception : ").append(e.getMessage());
		buf.append("SQL:").append(sql).append("\nParameters");
		final SQLException e1 = e.getNextException();
		if (e1 != null) {
			buf.append("\nLinked to the SqlExcpetion: ").append(e1.getMessage());
		}
		int idx = 1;
		for (final FormDbParam p : params) {
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
	public boolean fetch(final DbHandle handle) throws SQLException {
		final DbMetaData meta = this.form.dbMetaData;
		if (meta == null) {
			logger.error("{} is not designed for any db operation", this.form.getFormId());
			return false;
		}
		final boolean ok = fetchWorker(handle, meta.selectClause + meta.whereClause, this.fieldValues, meta.whereParams,
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
	public boolean fetchUsingUniqueKeys(final DbHandle handle) throws SQLException {
		final DbMetaData meta = this.form.dbMetaData;
		if (meta == null) {
			logger.error("{} is not designed for any db operation", this.form.getFormId());
			return false;
		}
		if (meta.uniqueClause == null) {
			logger.error("{} has no unque keys defined. can not be fetched with unique keys.", this.form.getFormId());
			return false;
		}
		final boolean ok = fetchWorker(handle, meta.selectClause + meta.uniqueClause, this.fieldValues,
				meta.uniqueParams, meta.selectParams);
		if (ok && meta.dbLinks != null) {
			this.fetchChildren(handle);
		}
		return ok;
	}

	private static boolean fetchWorker(final DbHandle driver, final String sql, final Object[] values,
			final FormDbParam[] setters, final FormDbParam[] getters) throws SQLException {
		final boolean[] result = new boolean[1];
		driver.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				if (setters == null || setters.length == 0) {
					return;
				}
				int posn = 1;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FormDbParam p : setters) {
					final Object value = values[p.idx];
					p.valueType.setPsParam(ps, posn, value);
					sbf.append('\n').append(posn).append('=').append(value);
					posn++;
				}
				logger.info(sbf.toString());
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				int posn = 1;
				for (final FormDbParam p : getters) {
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
	private void fetchChildren(final DbHandle handle) throws SQLException {
		int idx = 0;
		for (final DbLink link : this.form.dbMetaData.dbLinks) {
			if (link != null) {
				final Form childForm = this.form.childForms[idx].form;
				final DbMetaData meta = link.childMeta;
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
	private void insertChildren(final DbHandle handle) throws SQLException {
		int idx = -1;
		for (final DbLink link : this.form.dbMetaData.dbLinks) {
			idx++;
			if (link == null) {
				logger.error("Db Link missing for child {}", idx);
				continue;
			}

			final FormData[] rows = this.childData[idx];
			if (rows == null || rows.length == 0) {
				logger.error("Child form at {} has has no data", idx);
				continue;
			}

			final Form childForm = this.form.childForms[idx].form;
			final DbMetaData meta = link.childMeta;
			/*
			 * we copy parent key to children. keep them in an array for faster
			 * access.
			 */
			final int nbrKeys = link.linkParentParams.length;
			final Object[] parentKeys = new Object[nbrKeys];
			final int[] childKeyIdexes = new int[nbrKeys];

			for (int i = 0; i < nbrKeys; i++) {
				final FormDbParam parentParam = link.linkParentParams[i];
				parentKeys[i] = this.fieldValues[parentParam.idx];

				final Field childKey = childForm.getField(link.childLinkNames[i]);
				childKeyIdexes[i] = childKey.getIndex();
			}
			final int nbrRows = rows.length;

			handle.write(new IDbMultipleWriter() {
				int rowIdx = 0;

				@Override
				public String getPreparedStatement() {
					return meta.insertClause;
				}

				@Override
				public boolean setParams(final PreparedStatement ps) throws SQLException {
					final FormData fd = rows[this.rowIdx];
					/*
					 * copy parent keys to the child row
					 */
					for (int i = 0; i < nbrKeys; i++) {
						final Object k = parentKeys[i];
						fd.fieldValues[childKeyIdexes[i]] = k;
						logger.info("parent key {} copied to child", k);
					}

					final StringBuilder sbf = new StringBuilder("Parameter Values for batch row ").append(this.rowIdx);
					int posn = 0;
					for (final FormDbParam p : meta.insertParams) {
						posn++;
						final Object value = fd.fieldValues[p.idx];
						p.valueType.setPsParam(ps, posn, value);
						sbf.append('\n').append(posn).append('=').append(value);
					}
					logger.info(sbf.toString());

					/*
					 * return true if we have to add more. false if we are done
					 */
					this.rowIdx++;
					return (this.rowIdx < nbrRows);
				}
			});
		}
	}

	/**
	 * retrieves rows of data into an array of form-data
	 *
	 * @param handle
	 * @param form
	 * @param sql
	 *            complete sql
	 * @param values
	 *            for where clause (setting parameters to the prepared
	 *            statements
	 * @param setters
	 *            for setting ps
	 * @param getters
	 *            for getting data back from result set
	 * @return array of form data
	 * @throws SQLException
	 */
	public static FormData[] fetchDataWorker(final DbHandle handle, final Form form, final String sql,
			final Object[] values, final FormDbParam[] setters, final FormDbParam[] getters) throws SQLException {
		final List<FormData> result = new ArrayList<>();
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				int posn = 1;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FormDbParam p : setters) {
					final Object value = values[p.idx];
					p.valueType.setPsParam(ps, posn, value);
					sbf.append('\n').append(posn).append('=').append(value);
					posn++;
				}
				logger.info(sbf.toString());
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				final FormData fd = form.newFormData();
				result.add(fd);
				int posn = 1;
				for (final FormDbParam p : getters) {
					fd.fieldValues[p.idx] = p.valueType.getFromRs(rs, posn);
					posn++;
				}
				return true;
			}
		});
		return result.toArray(new FormData[0]);
	}

	/**
	 * if any validation fails,error message woudlhave been pushed to teh ctx
	 *
	 * @param values
	 *            string values for the form in the right order for the
	 *            fields.That is first element is for the first field in this
	 *            form etc.. It MUST have exactly the right number of values
	 * @param ctx
	 */
	public void validateAndLoadForInsert(final String[] values, final IServiceContext ctx) {

		int userIdx = this.form.getUserIdFieldIdx();
		for (final Field field : this.form.getFields()) {
			final int idx = field.getIndex();
			if (userIdx > -1 && field.getIndex() == userIdx) {
				this.setUserId(ctx.getUser());
				userIdx = -1; // we are done
				continue;
			}
			final ColumnType ct = field.getColumnType();
			if (ct != null) {
				if (!ct.isInput()) {
					continue;
				}
				if (ct == ColumnType.GeneratedPrimaryKey) {
					continue;
				}
				if (ct == ColumnType.ModifiedBy || ct == ColumnType.CreatedBy) {
					this.fieldValues[idx] = ctx.getUser().getUserId();
				}
			}

			final String value = values[idx];
			validateAndSet(field, value, this.fieldValues, field.getIndex(), false, ctx, null, 0);
		}
		this.validateForm(ctx);
	}

	/**
	 * log all field values
	 */
	public void logValues() {
		logger.info("Data for form {}", this.form.getFormId());
		int idx = -1;
		for (final Field field : this.form.fields) {
			idx++;
			logger.info("{} : {} = {}", field.getFieldName(), field.getDbColumnName(), this.fieldValues[idx]);
		}
	}
}
