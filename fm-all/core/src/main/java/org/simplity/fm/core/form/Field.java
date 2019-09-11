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

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.datatypes.InvalidValueException;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.validn.IValueList;

/**
 * @author simplity.org
 *
 */
public class Field {
	/**
	 * field name is unique within a form/template. However, it is strongly
	 * advised that the same name is used in different forms if they actually
	 * refer to the same data element
	 */
	private String fieldName;

	/**
	 * 0-based index of the field in the parent form;
	 */
	private int index;
	/**
	 * data type describes the type of value and restrictions (validations) on
	 * the value
	 */
	private DataType dataType;
	/**
	 * default value is used only if this is optional and the value is missing.
	 * not
	 * used if the field is mandatory
	 */
	private Object defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	private String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	private boolean isRequired;
	/**
	 * Is this field editable by the client. If false, then this can be either a
	 * "reference field" that is used for display or validation purposes. It is
	 * typically not sent back from client.
	 */
	private boolean isEditable;

	/**
	 * if this field has a list of valid values, either known at design time or
	 * at run-time.
	 * Note that this attribute is assigned ONLY if this list is not based on
	 * another field value. If it is based on another field value, it is managed
	 * as part of inter-field validations
	 */
	private String valueListName;

	/**
	 * cached value list for
	 */
	private IValueList valueList;

	/**
	 * db column name
	 */
	private String dbColumnName;

	/**
	 * what type of column, like key? time-stamp etc..
	 */
	private ColumnType columnType;

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
	 * @param isRequired
	 *            is this field mandatory. used for validating data coming from
	 *            a client
	 * @param defaultValue
	 *            value to be used in case the client has not sent a value for
	 *            this. This e is used ONLY if isRequired is false. That is,
	 *            this is used if the field is optional, and the client skips
	 *            it. This value is NOT used if isRequired is set to true
	 * @param isEditable
	 *            can this field be edited/requested by the client? If false,
	 *            the field is either a reference field or a derived field
	 * @param messageId
	 *            can be null in which case the id from dataType is used
	 * @param valueListName
	 *            if this field has a list of valid values that are typically
	 *            rendered in a drop-down. If the value list depends on value of
	 *            another field, then it is part of inter-field validation, and
	 *            not part of this field.
	 * @param dbColumnName
	 *            column name in the data base, if this is linked to one
	 * @param columnType
	 */
	public Field(String fieldName, int index, DataType dataType, String defaultValue, String messageId,
			boolean isRequired, boolean isEditable, String valueListName, String dbColumnName, ColumnType columnType) {
		this.fieldName = fieldName;
		this.index = index;
		this.isRequired = isRequired;
		this.isEditable = isEditable;
		this.messageId = messageId;
		if (defaultValue == null) {
			this.defaultValue = null;
		} else {
			this.defaultValue = dataType.parse(defaultValue);
		}
		this.dataType = dataType;
		if (valueListName == null) {
			this.valueListName = null;
			this.valueList = null;
		} else {
			this.valueListName = valueListName;
			this.valueList = ComponentProvider.getProvider().getValueList(valueListName);
		}
		this.dbColumnName = dbColumnName;
		this.columnType = columnType;
	}

	/**
	 * a field that has no validation/client related meta data
	 * 
	 * @param fieldName
	 * @param index
	 * @param dataType
	 * @param dbColumnName
	 * @param columnType
	 */
	public Field(String fieldName, int index, DataType dataType, String dbColumnName, ColumnType columnType) {
		this.fieldName = fieldName;
		this.index = index;
		this.dataType = dataType;
		this.dbColumnName = dbColumnName;
		this.columnType = columnType;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * @return the defaultValue
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the isRequired
	 */
	public boolean isRequired() {
		return this.isRequired;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		if (this.messageId != null) {
			return this.messageId;
		}
		return this.dataType.getMessageId();
	}

	/**
	 * @return is this field editable?
	 */
	public boolean isEditable() {
		return this.isEditable;
	}

	/**
	 * @return value type
	 */
	public ValueType getValueType() {
		return this.dataType.getValueType();
	}

	/**
	 * @return the dbColumnName
	 */
	public String getDbColumnName() {
		return this.dbColumnName;
	}

	/**
	 * 
	 * @return the column-type, null if this is not a db-column
	 */
	public ColumnType getColumnType() {
		return this.columnType;
	}

	/**
	 * parse into the desired type, validate and return the value. caller should
	 * check for exception for validation failure and not returned value as
	 * null.
	 * <br />
	 * Caller may opt not to check for mandatory condition by checking for null
	 * before calling this method. That is, in such a case, caller handles the
	 * null condition, and calls this only if it is not null.
	 * 
	 * @param inputValue
	 *            input text. can be null, in which case it is validated for
	 *            mandatory
	 * @return object of the right type. or null if the value is null and it is
	 *         valid
	 * @throws InvalidValueException
	 *             if the value is invalid.
	 */
	public Object parse(String inputValue) throws InvalidValueException {
		if (inputValue == null || inputValue.isEmpty()) {
			if (this.isRequired == false) {
				return null;
			}
			this.throwMessage();
		}
		Object obj = this.dataType.parse(inputValue);
		if (obj == null) {
			this.throwMessage();
		}

		if (this.valueList != null && this.valueList.isValid(inputValue, null) == false) {
			this.throwMessage();
		}
		return obj;
	}

	private void throwMessage() throws InvalidValueException {
		throw new InvalidValueException(this.getMessageId(), this.fieldName);

	}

	/**
	 * is this a key field?
	 * 
	 * @return true if this is the key field, or one of the key fields
	 */
	public boolean isKeyField() {
		if (this.columnType == null) {
			return false;
		}
		return this.columnType == ColumnType.PrimaryKey 
				|| this.columnType == ColumnType.GeneratedPrimaryKey
				|| this.columnType == ColumnType.PrimaryAndParentKey;
	}

	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return this.dataType;
	}

	/**
	 * @return the valueListName
	 */
	public String getValueListName() {
		return this.valueListName;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}
}
