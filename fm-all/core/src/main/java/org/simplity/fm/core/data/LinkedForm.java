/*
<<<<<<< HEAD
 * Copyright (c) 2020 simplity.org
=======
 * Copyright (c) 2019 simplity.org
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
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

<<<<<<< HEAD
import java.sql.SQLException;

import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.rdb.ReadWriteHandle;
import org.simplity.fm.core.rdb.ReadonlyHandle;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.service.IServiceContext;

/**
 * represents a child/linked form for a parent form
 *
 * @author simplity.org
 * @param <T>
 *            dbRecord of the linked form
 *
 */
public class LinkedForm<T extends DbRecord> {
	/**
	 * how this form is linked to its parent
	 */
	private final LinkMetaData linkMeta;

	private final Form<T> form;

	/**
	 *
	 * @param linkMeta
	 * @param form
	 */
	public LinkedForm(final LinkMetaData linkMeta, final Form<T> form) {
		this.linkMeta = linkMeta;
		this.form = form;
	}

	/**
	 * read rows for this linked form based on the parent record
	 *
	 * @param parentRec
	 *            parent record
	 * @param writer
	 *            to which data is to be serialized to
	 * @param handle
	 * @throws SQLException
	 */
	public void read(final DbRecord parentRec, final ISerializer writer, final ReadonlyHandle handle)
			throws SQLException {
		this.linkMeta.read(parentRec, this.form, writer, handle);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean insert(final DbRecord parentRec, final IInputObject inputObject, final ReadWriteHandle handle,
			final IServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.save(parentRec, this.form, inputObject, handle, ctx);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean update(final DbRecord parentRec, final IInputObject inputObject, final ReadWriteHandle handle,
			final IServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.save(parentRec, this.form, inputObject, handle, ctx);
	}

	/**
	 * @param parentRec
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean delete(final DbRecord parentRec, final ReadWriteHandle handle, final IServiceContext ctx)
			throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.delete(parentRec, this.form, handle);
	}

	private void checkUpdatability() {
		if (this.form.hasLinks()) {
			throw new ApplicationError(
					"Auto delete operation not allowed on a form with hrand-links. It can have only one level of linked forms.");
		}
	}

	/**
	 * must be called by parent form before it is used
	 *
	 * @param parentRecord
	 */
	public void init(final Record parentRecord) {
		this.linkMeta.init(parentRecord, this.form.getRecord());

	}
=======
import java.io.IOException;
import java.sql.SQLException;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.service.IServiceContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * this is used in generated classes
 *
 * @author simplity.org
 *
 */
public class LinkedForm {
	/**
	 * non-null unique across all fields of the form
	 */
	protected String linkName;

	/**
	 * name of the other form being linked
	 */
	protected String linkFormName;

	/**
	 * if this is tabular, min rows expected from client
	 */
	protected int minRows;
	/**
	 * if this is tabular, max rows expected from client.
	 */
	protected int maxRows;
	/**
	 * field names from the parent form that are used for linking
	 */
	protected String[] parentLinkNames;
	/**
	 * field names from the child form that form the parent-key for the child
	 * form
	 */
	protected String[] childLinkNames;
	/**
	 * in case min/max rows violated, what is the error message to be used to
	 * report this problem
	 */
	protected String errorMessageId;

	/*
	 * fields that are created at init()
	 */

	/**
	 * form that is linked
	 */
	protected Form linkedForm;

	/**
	 * in case the schemas are to be linked, then we need the where clause where
	 * the column names come from the linked schema,while the values for them
	 * come from the parent schema
	 * e.g. where childCol1=? and childCll2=?
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

	/**
	 * called by parent form/schema before it is used.
	 *
	 * @param parentSchema
	 */
	void init(final Schema parentSchema) {
		this.linkedForm = ComponentProvider.getProvider().getForm(this.linkFormName);
		if (this.childLinkNames == null || this.childLinkNames.length == 0) {
			return;
		}

		final StringBuilder sbf = new StringBuilder(" WHERE ");
		final int nbr = this.parentLinkNames.length;
		this.parentIndexes = new int[nbr];
		this.childIndexes = new int[nbr];
		this.linkWhereParams = new FieldMetaData[nbr];
		final Schema linkedSchema = this.linkedForm.getSchema();
		for (int i = 0; i < nbr; i++) {
			final DbField parentField = parentSchema.getField(this.parentLinkNames[i]);
			final DbField childField = linkedSchema.getField(this.childLinkNames[i]);
			this.parentIndexes[i] = parentField.index;
			this.childIndexes[i] = childField.index;
			if (i != 0) {
				sbf.append(" AND ");
			}
			sbf.append(childField.columnName).append("=?");
			this.linkWhereParams[i] = new FieldMetaData(parentField);
		}

		this.linkWhereClause = sbf.toString();
		this.deleteSql = "delete from " + linkedSchema.nameInDb + this.linkWhereClause;
	}

	/**
	 *
	 * @param handle
	 * @param parentRow
	 * @return non-null data table with data extracted from the db
	 * @throws SQLException
	 */
	public SchemaDataTable read(final DbHandle handle, final Object[] parentRow) throws SQLException {
		final PreparedStatementParam[] params = this.createParams(parentRow);
		return this.linkedForm.getSchema().filterToTable(handle, this.linkWhereClause, params);
	}

	private PreparedStatementParam[] createParams(final Object[] parentRow) {
		final PreparedStatementParam[] params = new PreparedStatementParam[this.linkWhereParams.length];
		int idx = -1;
		for (final FieldMetaData p : this.linkWhereParams) {
			idx++;
			params[idx] = new PreparedStatementParam(parentRow[p.getIndex()], p.getValueType());
		}
		return params;
	}

	/**
	 *
	 * @param json
	 *            that has the json-array for this form
	 * @param forInsert
	 *            if the rows are to be parsed for insert (generated primary key
	 *            is optional in the case of insert)
	 * @param ctx
	 *            any validation error is added to it
	 * @return null in case of any error in the input data.
	 */
	public FormDataTable parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx) {
		final JsonArray arr = json.getAsJsonArray(this.linkName);
		FormDataTable data = null;
		int nbr = 0;
		if (arr != null && arr.size() > 0) {
			data = this.linkedForm.parseTable(arr, forInsert, ctx, this.linkName);
			if (ctx.allOk() == false) {
				return null;
			}
			if (data != null) {
				nbr = data.length();
			}
		}
		if (this.minRows > nbr) {
			ctx.addMessage(Message.newFieldError(this.linkName, "minRows", "" + this.minRows));
			return null;
		}
		if (this.maxRows != 0 && nbr > this.maxRows) {
			ctx.addMessage(Message.newFieldError(this.linkName, "maxRows", "" + this.maxRows));
			return null;
		}
		return data;
	}

	/**
	 * @param handle
	 * @param dataTable
	 * @param dataRow
	 * @return true of every row is updated. false otherwise
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle, final SchemaDataTable dataTable, final Object[] dataRow)
			throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.fieldValues);
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
	public boolean insert(final DbHandle handle, final SchemaDataTable dataTable, final Object[] dataRow)
			throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.fieldValues);
		return dataTable.insert(handle);
	}

	/**
	 * @param handle
	 * @param dataTable
	 * @param dataRow
	 * @return true of every row is updated. false otherwise
	 * @throws SQLException
	 */
	public boolean save(final DbHandle handle, final SchemaDataTable dataTable, final Object[] dataRow)
			throws SQLException {
		/*
		 * we copy parent key into child key to ensure that the update process
		 * is not changing the parent
		 */
		this.copyKeys(dataRow, dataTable.fieldValues);
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
		handle.write(this.deleteSql, params);
		return true;
	}

	/**
	 * @param table
	 * @param writer
	 * @throws IOException
	 */
	public void serializeToJson(final FormDataTable table, final JsonWriter writer) throws IOException {
		if (table == null) {
			return;
		}
		writer.name(this.linkName);
		writer.beginArray();
		for (final FormData fd : table) {
			writer.beginObject();
			fd.serializeFields(writer);
			writer.endObject();
		}
		writer.endArray();
	}

>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
}
