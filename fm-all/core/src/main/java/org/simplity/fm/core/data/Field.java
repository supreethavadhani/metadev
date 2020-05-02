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

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Field {
	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	/**
	 * name is unique within a record/form
	 */
	protected final String name;

	/**
	 * 0-based index of the field in the record;
	 */
	protected final int index;
	/**
	 * data type describes the type of value and restrictions (validations) on
	 * the value
	 */
	protected final DataType dataType;
	/**
	 * default value is used only if this is optional and the value is missing.
	 * not used if the field is mandatory
	 */
	protected final Object defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	protected final String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	protected final boolean isRequired;

	/**
	 * cached value list for
	 */
	protected final IValueList valueList;

	/**
	 * this is generally invoked by the generated code for a Data Structure
	 *
	 * @param fieldName
	 *            unique within its data structure
	 * @param index
	 *            0-based index of this field in the prent form
	 * @param dataType
	 *            pre-defined data type. used for validating data coming from a
	 *            client
	 * @param defaultValue
	 *            value to be used in case the client has not sent a value for
	 *            this. This e is used ONLY if isRequired is false. That is,
	 *            this is used if the field is optional, and the client skips
	 *            it. This value is NOT used if isRequired is set to true
	 * @param messageId
	 *            can be null in which case the id from dataType is used
	 * @param valueListName
	 *            if this field has a list of valid values that are typically
	 *            rendered in a drop-down. If the value list depends on value of
	 *            another field, then it is part of inter-field validation, and
	 *            not part of this field.
	 * @param isRequired
	 *            is this field mandatory. used for validating data coming from
	 *            a client
	 */
	public Field(final String fieldName, final int index, final DataType dataType, final String defaultValue,
			final String messageId, final String valueListName, final boolean isRequired) {
		this.name = fieldName;
		this.index = index;
		this.isRequired = isRequired;
		this.messageId = messageId;
		if (defaultValue == null) {
			this.defaultValue = null;
		} else {
			this.defaultValue = dataType.parse(defaultValue);
		}
		this.dataType = dataType;
		if (valueListName == null) {
			this.valueList = null;
		} else {
			this.valueList = ComponentProvider.getProvider().getValueList(valueListName);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return this.dataType;
	}

	/**
	 * @return the defaultValue
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		if (this.messageId == null) {
			return this.dataType.getMessageId();
		}
		return this.messageId;
	}

	/**
	 * @param forInsert
	 *            if the operation is insert
	 * @return the isRequired
	 */
	public boolean isRequired(final boolean forInsert) {
		return this.isRequired;
	}

	/**
	 *
	 * @return the value type of this field
	 */
	public ValueType getValueType() {
		return this.dataType.getValueType();
	}

	/**
	 * @return true if this field is the tenant key.
	 */
	public boolean isTenantKey() {
		return false;
	}

	/**
	 * @return true if this field is the tenant key.
	 */
	public boolean isPrimaryKey() {
		return false;
	}

	/**
	 * @return true if this field is user id, like createdBy and modifiedBy.
	 */
	public boolean isUserId() {
		return false;
	}

	/**
	 * validate if null/empty-string is ok for this field
	 *
	 * @param forInsert
	 * @param ctx
	 *            non-null
	 * @param tableName
	 * @param idx
	 * @return true if it is ok. false if an error message has been added to ctx
	 */
	public boolean validateNull(final boolean forInsert, final IServiceContext ctx, final String tableName,
			final int idx) {
		if (this.isRequired(forInsert)) {
			logger.error("Field {} is required but no data is received", this.name);
			ctx.addMessage(Message.newValidationError(this, tableName, idx));
			return false;
		}
		return true;
	}

	/**
	 * parse into the desired type, validate and return the value. Meant to be
	 * called after validating null input for mandatory condition
	 *
	 * @param inputValue
	 *            non-null. input text.
	 * @param ctx
	 *            can be null. error added if not null;
	 * @param tableName
	 * @param idx
	 * @return object of the right type. or null if the value is invalid
	 */
	public Object parse(final String inputValue, final IServiceContext ctx, final String tableName, final int idx) {
		final Object obj = this.dataType.parse(inputValue);
		if (obj == null) {
			logger.error("{} is not valid for field {} as per data type {}", inputValue, this.name,
					this.dataType.getName());
			ctx.addMessage(Message.newValidationError(this, tableName, idx));
			return null;
		}

		if (this.valueList != null && this.valueList.isValid(obj, null, ctx) == false) {
			logger.error("{} is not found in the list of valid values for  for field {}", inputValue, this.name);
			ctx.addMessage(Message.newValidationError(this, tableName, idx));
			return null;
		}
		return obj;
	}

	/**
	 *
	 * @param value
	 *            string value that is to be parsed. can be null or empty
	 * @param row
	 *            into which parsed values is to be set to. MUST be array with
	 *            the right number of elements
	 * @param forInsert
	 *            true if this parsing is for an insert operation
	 * @param ctx
	 *            into which any error message is added
	 * @param tableName
	 *            if this row is inside a table. used for reporting error
	 * @param rowNbr
	 *            used for reporting error is this is part of table
	 */
	public void parseIntoRow(final String value, final Object[] row, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		/*
		 * do we have to set value from our side?
		 */
		if (this.isTenantKey()) {
			row[this.index] = ctx.getTenantId();
			return;
		}

		if (this.isUserId()) {
			row[this.index] = ctx.getUser().getUserId();
			return;
		}

		if (value == null || value.isEmpty()) {
			row[this.index] = null;
			this.validateNull(forInsert, ctx, tableName, rowNbr);
			return;
		}

		row[this.index] = this.parse(value, ctx, tableName, rowNbr);
	}

	/**
	 *
	 * @return column name if this is relevant. null if column name is not
	 *         relevant. Is non-null in case of DbField instances
	 */
	public Object getColumnName() {
		return null;
	}
}
