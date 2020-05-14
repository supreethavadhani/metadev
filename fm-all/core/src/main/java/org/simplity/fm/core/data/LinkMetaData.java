/*
 * Copyright (c) 2020 simplity.org
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

/**
 * represents meta data for a linked form
 *
 * @author simplity.org
 *
 */
public class LinkMetaData {
	/**
	 * non-null unique across all fields of the form
	 */
	protected final String linkName;

	/**
	 * name of the other form being linked
	 */
	protected final String linkFormName;

	/**
	 * if this is tabular, min rows expected from client
	 */
	protected final int minRows;
	/**
	 * if this is tabular, max rows expected from client.
	 */
	protected final int maxRows;
	/**
	 * field names from the parent form that are used for linking
	 */
	protected final String[] parentLinkNames;
	/**
	 * field names from the child form that form the parent-key for the child
	 * form
	 */
	protected final String[] childLinkNames;
	/**
	 * in case min/max rows violated, what is the error message to be used to
	 * report this problem
	 */
	protected final String errorMessageId;

	/**
	 * is the link meant for an array of data or 1-to-1?
	 */
	protected final boolean isTabular;
	/*
	 * fields that are created at init()
	 */

	/**
	 * in case the schemas are to be linked, then we need the where clause where
	 * the column names come from the linked schema, while the values for them
	 * come from the parent schema
	 * e.g. childCol1=? and childCll2=?
	 */
	protected String linkWhereClause;

	/**
	 * has the details to set params values for a prepared statement from a
	 * parent data row
	 */
	protected FieldMetaData[] linkWhereParams;
	/**
	 * in case the linked schema is to be used for deleting children
	 */
	protected String deleteSql;

	/**
	 * how do we link the parent and the child/linked schema?
	 */
	protected int[] parentIndexes;
	protected int[] childIndexes;

	protected LinkMetaData(final String linkName, final String linkFormName, final int minRows, final int maxRows,
			final String errorMessageId, final String[] parentLinkNames, final String[] childLinkNames,
			final boolean isTabular) {
		this.linkName = linkName;
		this.linkFormName = linkFormName;
		this.minRows = minRows;
		this.maxRows = maxRows;
		this.parentLinkNames = parentLinkNames;
		this.childLinkNames = childLinkNames;
		this.errorMessageId = errorMessageId;
		this.isTabular = isTabular;
	}

	/**
	 * called by parent form/schema before it is used.
	 *
	 * @param parentSchema
	 */
	void init(final DbRecord parentRecord, final DbRecord childRecord) {
		final StringBuilder sbf = new StringBuilder(" WHERE ");
		final int nbr = this.parentLinkNames.length;
		this.parentIndexes = new int[nbr];
		this.childIndexes = new int[nbr];
		this.linkWhereParams = new FieldMetaData[nbr];

		for (int i = 0; i < nbr; i++) {
			final DbField parentField = parentRecord.getField(this.parentLinkNames[i]);
			/*
			 * child field name is not verified during generation... we may get
			 * run-time exception
			 */
			final DbField childField = childRecord.getField(this.childLinkNames[i]);
			if (childField == null) {
				throw new RuntimeException("Field " + this.childLinkNames[i]
						+ " is defined as childLinkName, but is not defined as a field in the linked form "
						+ this.linkFormName);
			}
			this.parentIndexes[i] = parentField.index;
			this.childIndexes[i] = childField.index;
			if (i != 0) {
				sbf.append(" AND ");
			}
			sbf.append(childField.columnName).append("=?");
			this.linkWhereParams[i] = new FieldMetaData(parentField);
		}

		this.linkWhereClause = sbf.toString();
		this.deleteSql = "delete from " + childRecord.dba.nameInDb + this.linkWhereClause;
	}
}
