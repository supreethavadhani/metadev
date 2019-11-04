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

import org.simplity.fm.core.datatypes.DataType;

/**
 * @author simplity.org
 *
 */
public class DbField extends Field {

	/**
	 * name of the db column
	 */
	protected final String columnName;

	/**
	 * type of column
	 */
	protected final ColumnType columnType;

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
	 * @param messageId
	 *            can be null in which case the id from dataType is used
	 * @param valueListName
	 *            if this field has a list of valid values that are typically
	 *            rendered in a drop-down. If the value list depends on value of
	 *            another field, then it is part of inter-field validation, and
	 *            not part of this field.
	 * @param columnName
	 *            db column name. non-null
	 * @param columnType
	 *            db column type. non-null
	 */
	public DbField(final String fieldName, final int index, final DataType dataType, final String defaultValue,
			final String messageId, final boolean isRequired, final String valueListName, final String columnName,
			final ColumnType columnType) {
		super(fieldName, index, dataType, defaultValue, messageId, columnType.isRequired(), valueListName);
		this.columnName = columnName;
		this.columnType = columnType;
	}

	@Override
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * @return the column type
	 */
	public ColumnType getColumnType() {
		return this.columnType;
	}

	@Override
	public boolean isRequired(final boolean forInsert) {
		if (forInsert && this.columnType == ColumnType.GeneratedPrimaryKey) {
			return false;
		}
		return this.columnType.isRequired();
	}

	/**
	 * @return true if this column is part of the primary key
	 */
	@Override
	public boolean isPrimaryKey() {
		return this.columnType == ColumnType.PrimaryKey || this.columnType == ColumnType.GeneratedPrimaryKey;
	}

	@Override
	public boolean isTenantKey() {
		return this.columnType == ColumnType.TenantKey;
	}

	@Override
	public boolean isUserId() {
		return this.columnType == ColumnType.ModifiedBy || this.columnType == ColumnType.CreatedBy;
	}
}
