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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.form.DbMetaData;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents an RDBMS table/view
 *
 * @author simplity.org
 *
 */
public class DbSchema {
	protected static final Logger logger = LoggerFactory.getLogger(DbSchema.class);
	/**
	 * name must be unique across tables and views
	 */
	protected String name;
	/**
	 * columns in this table.
	 */
	protected DbField[] fields;

	/**
	 * describes all the inter-field validations, and any business validations
	 */
	protected IValidation[] validations;

	/**
	 * primary key column/s. most of the times, it is one field that is
	 * internally generated
	 */
	protected int[] keyIndexes;
	/**
	 * columns are also stored as Maps for ease of access
	 */
	protected Map<String, DbField> fieldsMap;

	/**
	 * meta data required for db operations. null if this is not designed for db
	 * operations
	 */
	protected DbMetaData dbMetaData;

	/**
	 * MUST BE CALLED after setting all protected fields
	 */
	protected void initialize() {
		final int n = this.fields.length;
		this.fieldsMap = new HashMap<>(n, 1);
		final int[] keys = new int[n];
		int keyIdx = 0;
		for (final DbField column : this.fields) {
			this.fieldsMap.put(column.getName(), column);
			if (column.isPrimaryKey()) {
				keys[keyIdx] = column.getIndex();
				keyIdx++;
			}
		}
		if (keyIdx != 0) {
			this.keyIndexes = Arrays.copyOf(keys, keyIdx);
		}
	}

	/**
	 * @return the keyIndexes
	 */
	public int[] getKeyIndexes() {
		return this.keyIndexes;
	}

	/**
	 *
	 * @param columnName
	 * @return column or null if no such column
	 */
	public DbField getField(final String columnName) {
		return this.fieldsMap.get(columnName);
	}

	/**
	 *
	 * @param idx
	 *            0 based index
	 * @return column or null if index is <= 0 or >= nbr fields
	 */
	public DbField getField(final int idx) {
		if (idx >= 0 && idx <= this.fields.length) {
			return this.fields[idx];
		}
		return null;
	}

	/**
	 * @return the validations
	 */
	public IValidation[] getValidations() {
		return this.validations;
	}

	/**
	 * @return number of columns in this table
	 */
	public int getNbrFields() {
		return this.fields.length;
	}

	/**
	 * @return the fields
	 */
	public DbField[] getFields() {
		return this.fields;
	}

	/**
	 *
	 * @return a db data that can carry data for this schema
	 */
	public DbData newDbData() {
		return new DbData(this);
	}
}
