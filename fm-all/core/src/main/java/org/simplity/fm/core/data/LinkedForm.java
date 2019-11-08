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

import java.sql.SQLException;

import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.service.IServiceContext;

import com.google.gson.JsonArray;

/**
 * @author simplity.org
 *
 */
public class LinkedForm {
	/**
	 * non-null unique across all fields of the form
	 */
	protected final String linkName;
	/**
	 * form being linked
	 */
	protected final Schema schema;
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
	protected String[] childLinkNames;
	/**
	 * in case min/max rows violated, what is the error message to be used to
	 * report this problem
	 */
	protected final String errorMessageId;

	/**
	 * column names are from the child table, but the values for the parameter
	 * would come from the parent form
	 * e.g. where childCol1=? and childCll2=?
	 */
	protected String linkWhereClause;

	protected int[] parentIndexes;

	protected int[] childIndexes;

	/**
	 * has the details to set params values for a prepared statement from a
	 * parent data row
	 */
	protected FieldMetaData[] linkWhereParams;
	protected String deletePart = "delete etc...";

	/**
	 *
	 * @param linkName
	 *            non-null unique across all fields of the form
	 * @param schema
	 *            non-null name of the child schema
	 * @param minRows
	 *            for validation of data
	 * @param maxRows
	 *            for validation of data. though 0 means unlimited, we strongly
	 *            encourage a reasonable limit
	 * @param childLinkNames
	 * @param parentLinkNames
	 * @param linkWhereClause
	 * @param errorMessageId
	 *            message id to be used if number of data rows fails validation
	 */
	public LinkedForm(final String linkName, final Schema schema, final int minRows, final int maxRows,
			final String[] childLinkNames, final String[] parentLinkNames, final String linkWhereClause,
			final String errorMessageId) {
		this.linkName = linkName;
		this.schema = schema;
		this.minRows = minRows;
		this.maxRows = maxRows;
		this.errorMessageId = errorMessageId;
		this.childLinkNames = childLinkNames;
		this.parentLinkNames = parentLinkNames;

		// this.linkWhereClause = linkWhereClause;
		this.linkWhereParams = null;
	}

	/**
	 *
	 * @param handle
	 * @param parentRow
	 * @return non-null data table with data extracted from the db
	 * @throws SQLException
	 */
	public DataTable fetch(final DbHandle handle, final Object[] parentRow) throws SQLException {
		final PreparedStatementParam[] params = this.createParams(parentRow);

		final Object[][] data = this.schema.getDbMetaData().filter(handle, this.linkWhereClause, params);
		if (data.length == 0) {
			return new DataTable(this.schema);
		}
		return new DataTable(this.schema, data);
	}

	private PreparedStatementParam[] createParams(final Object[] parentRow) {
		final PreparedStatementParam[] params = new PreparedStatementParam[this.linkWhereParams.length];
		int idx = -1;
		for (final FieldMetaData p : this.linkWhereParams) {
			idx++;
			params[idx] = new PreparedStatementParam(parentRow[p.idx], p.valueType);
		}
		return params;
	}

	/**
	 *
	 * @param arr
	 *            json array that has the input data
	 * @param forInsert
	 *            if the rows are to be parsed for insert (generated primary key
	 *            is optional in the case of insert)
	 * @param ctx
	 *            any validation error is added to it
	 * @return null in case of any error in the input data.
	 */
	public DataTable parse(final JsonArray arr, final boolean forInsert, final IServiceContext ctx) {
		return this.schema.parseTable(arr, forInsert, ctx, this.linkName);
	}

	/**
	 * @param handle
	 * @param dataTable
	 * @param dataRow
	 * @return true of every row is updated. false otherwise
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle, final DataTable dataTable, final Object[] dataRow)
			throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.dataTable);
		return dataTable.update(handle);
	}

	private void copyKeys(final Object[] parentRow, final Object[][] childRows) {
		int idx = -1;
		for (final int parentIdx : this.parentIndexes) {
			idx++;
			final Object value = parentRow[parentIdx];
			final int childIdx = this.childIndexes[idx];
			for (final Object[] row : childRows) {
				row[childIdx] = value;
			}
		}
	}

	/**
	 * @param handle
	 * @param dataTable
	 * @param dataRow
	 * @return true of every row is updated. false otherwise
	 * @throws SQLException
	 */
	public boolean insert(final DbHandle handle, final DataTable dataTable, final Object[] dataRow)
			throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.dataTable);
		return dataTable.insert(handle);
	}

	/**
	 * @param handle
	 * @param dataTable
	 * @param dataRow
	 * @return true of every row is updated. false otherwise
	 * @throws SQLException
	 */
	public boolean save(final DbHandle handle, final DataTable dataTable, final Object[] dataRow) throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.dataTable);
		return dataTable.save(handle);
	}

	/**
	 * @param handle
	 * @param dataRow
	 * @return true if all ok. false otherwise
	 * @throws SQLException
	 */
	public boolean delete(final DbHandle handle, final Object[] dataRow) throws SQLException {
		final PreparedStatementParam[] params = this.createParams(dataRow);
		final String sql = this.deletePart + this.linkWhereClause;
		handle.write(sql, params);
		return true;
	}

}
