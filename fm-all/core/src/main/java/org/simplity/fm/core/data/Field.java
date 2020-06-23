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

import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A field represents an atomic data element in the application. It has the meta
 * data to parse, validate and serialize a value meant for this data element
 *
 * @author simplity.org
 *
 */
public class Field {
	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	/**
	 * name is unique within a record/form
	 */
	private final String name;

	/**
	 * 0-based index of the field in the record. T;
	 */
	private final int index;
	/**
	 * data type describes the type of value and restrictions (validations) on
	 * the value
	 */
	private final DataType dataType;
	/**
	 * default value is used only if this is optional and the value is missing.
	 * not used if the field is mandatory
	 */
	private final Object defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	private final String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	private final boolean isRequired;

	/**
	 * cached value list for validations
	 */
	private final IValueList valueList;

	/**
	 * this is generally invoked by the generated code for a Data Structure
	 *
	 * @param fieldName
	 *            unique within its data structure
	 * @param index
	 *            0-based index of this field in the parent form
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
		this.dataType = dataType;
		if (defaultValue == null) {
			this.defaultValue = null;
		} else {
			this.defaultValue = dataType.parse(defaultValue);
		}
		if (valueListName == null) {
			this.valueList = null;
		} else {
			this.valueList = App.getApp().getCompProvider().getValueList(valueListName);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
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
	 * @return the messageId
	 */
	public String getMessageId() {
		if (this.messageId == null) {
			return this.dataType.getMessageId();
		}
		return this.messageId;
	}

	/**
	 * @return the isRequired
	 */
	public boolean isRequired() {
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
	 *
	 * @param value
	 *            string value that is to be parsed. can be null or empty
	 * @param row
	 *            into which parsed values is to be set to. MUST be array with
	 *            the right number of elements
	 * @param ctx
	 *            into which any error message is added
	 * @param tableName
	 *            if this row is inside a table. used for reporting error
	 * @param rowNbr
	 *            used for reporting error is this is part of table
	 * @return true if all ok. false if an error message is added to the context
	 */
	public boolean parseIntoRow(final String value, final Object[] row, final IServiceContext ctx,
			final String tableName, final int rowNbr) {

		if (value == null || value.isEmpty()) {
			row[this.index] = null;
			if (this.isRequired) {
				logger.error("Field {} is required but no data is received", this.name);
				ctx.addMessage(Message.newValidationError(this, tableName, rowNbr));
				return false;
			}
			return true;
		}

		final Object val = this.parse(value, ctx, tableName, rowNbr);
		row[this.index] = val;
		return val != null;
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
	private Object parse(final String inputValue, final IServiceContext ctx, final String tableName, final int idx) {
		final Object obj = this.dataType.parse(inputValue);
		if (obj == null) {
			logger.error("{} is not valid for field {} as per data type {}", inputValue, this.name,
					this.dataType.getName());
			ctx.addMessage(Message.newValidationError(this, tableName, idx));
			return null;
		}

		if (this.valueList == null) {
			return obj;
		}
		/*
		 * numeric 0 is generally considered as "not entered". This is handled
		 * by allowing 0 as part of dataType definition. One issue is when this
		 * is a valueList. Let us handle that specifically
		 */
		if (this.getValueType().equals(ValueType.Integer) && this.isRequired == false && ((Long) obj) == 0) {
			return obj;
		}

		if (this.valueList.isValid(obj, null, ctx)) {
			return obj;
		}

		logger.error("{} is not found in the list of valid values for  for field {}", inputValue, this.name);
		ctx.addMessage(Message.newValidationError(this, tableName, idx));
		return null;

	}
}
