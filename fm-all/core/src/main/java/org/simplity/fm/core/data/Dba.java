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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.rdb.IDbWriter;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages persistence related functionality for a <code>DbRecord</code>
 *
 * @author simplity.org
 *
 */
public class Dba {

	protected static final Logger logger = LoggerFactory.getLogger(Dba.class);
	/**
	 * table/view name in the database
	 */
	protected String nameInDb;

	/**
	 * fields that are mapped to the db. This is same as fields in the record,
	 * except any non-db-fields are replaced with null.
	 */
	protected DbField[] dbFields;

	/**
	 * primary key column/s. most of the times, it is one field that is
	 * internally generated
	 */
	protected int[] keyIndexes;
	/**
	 * e.g. where a=? and b=?
	 */
	protected String whereClause;
	/**
	 * db parameters to be used for the where clause
	 */
	protected FieldMetaData[] whereParams;
	/**
	 * e.g. select a,b,c from t
	 */
	protected String selectClause;
	/**
	 * db parameters to be used to receive data from the result set of the
	 * select query
	 */
	protected FieldMetaData[] selectParams;
	/**
	 * e.g insert a,b,c,d into table1 values(?,?,?,?)
	 */
	protected String insertClause;
	/**
	 * db parameters for the insert sql
	 */
	protected FieldMetaData[] insertParams;

	/**
	 * e.g. update table1 set a=?, b=?, c=?
	 */
	protected String updateClause;
	/**
	 * db parameters for the update sql
	 */
	protected FieldMetaData[] updateParams;

	/**
	 * e.g. delete from table1. Note that where is not part of this.
	 */
	protected String deleteClause;

	/**
	 * db column name that is generated as internal key. null if this is not
	 * relevant
	 */
	protected String generatedColumnName;

	/**
	 *
	 */
	protected int generatedKeyIdx;

	/**
	 * if this APP is designed for multi-tenant deployment, and this table has
	 * data across tenants..
	 */
	protected DbField tenantField;

	/**
	 * if this table allows update, and needs to use time-stamp-match technique
	 * to avoid concurrent updates..
	 */
	protected DbField timestampField;

	/**
	 * number of fields in the schema to which this meta data is attached
	 */
	protected int nbrFieldsInARow;

	/**
	 * return the select clause (like select a,b,...) without the where clause
	 * for this schema
	 *
	 * @return string that is a valid select-part of a sql that can be used with
	 *         a were clause to filter rows from the underlying dbtable.view
	 */
	public String getSelectClause() {
		return this.selectClause;
	}

	/**
	 * insert/create this form data into the db.
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	boolean insert(final DbHandle handle, final Object[] values) throws SQLException {
		int n = 0;
		if (this.generatedColumnName == null) {
			n = writeWorker(handle, this.insertClause, this.insertParams, values);
			return n > 0;
		}

		try {
			final long[] generatedKeys = new long[1];
			n = handle.insertAndGenerateKey(getWriter(this.insertClause, this.insertParams, values),
					this.generatedColumnName, generatedKeys);
			final long id = generatedKeys[0];
			if (id == 0) {
				logger.error("DB handler did not return generated key");
			} else {
				values[this.generatedKeyIdx] = generatedKeys[0];
				logger.info("Generated key {} assigned back to form data", id);
			}
			return n > 0;
		} catch (final SQLException e) {
			final String msg = toMessage(e, this.insertClause, this.insertParams, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
	}

	/**
	 * update this form data back into the db.
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	boolean update(final DbHandle handle, final Object[] values) throws SQLException {
		final int nbr = writeWorker(handle, this.updateClause, this.updateParams, values);
		return nbr > 0;
	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	boolean delete(final DbHandle handle, final Object[] values) throws SQLException {
		final String sql = this.deleteClause + this.whereClause;
		final int nbr = writeWorker(handle, sql, this.whereParams, values);
		return nbr > 0;
	}

	private static int writeWorker(final DbHandle handle, final String sql, final FieldMetaData[] params,
			final Object[] values) throws SQLException {
		try {
			return handle.write(getWriter(sql, params, values));
		} catch (final SQLException e) {
			final String msg = toMessage(e, sql, params, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
	}

	private static IDbWriter getWriter(final String sql, final FieldMetaData[] params, final Object[] values) {
		return new IDbWriter() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public boolean setParams(final PreparedStatement ps) throws SQLException {
				int posn = 0;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FieldMetaData p : params) {
					posn++;
					final Object value = p.setPsParam(ps, values, posn);
					sbf.append('\n').append(posn).append('=').append(value);
				}
				logger.info(sbf.toString());
				return true;
			}

		};
	}

	/**
	 * save all rows into the db. A row is inserted if it has no primary key,
	 * and is updated if it has primary key.
	 * NOTE: if any of the operation fails, we return a false. this may be used
	 * to roll-back the transaction.
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if all ok. false in case of any problem, caller should
	 *         roll-back if this is false
	 * @throws SQLException
	 */
	boolean saveAll(final DbHandle handle, final Object[][] rows) throws SQLException {
		if (this.generatedKeyIdx == -1) {
			logger.info("Schema has no generated key. Each rowis first updated, failing which it is inserted.");
			return this.updateOrInsert(handle, rows);
		}
		final int nbrRows = rows.length;
		/*
		 * we create array with max length rather than list
		 */
		Object[][] inserts = new Object[nbrRows][];
		Object[][] updates = new Object[nbrRows][];
		int nbrInserts = 0;
		int nbrUpdates = 0;

		for (final Object[] row : rows) {
			final Object key = row[this.generatedKeyIdx];
			if (key != null && ((Long) key) != 0) {
				updates[nbrUpdates] = row;
				nbrUpdates++;
			} else {
				inserts[nbrInserts] = row;
				nbrInserts++;
			}
		}

		if (nbrUpdates == 0) {
			return this.insertAll(handle, rows);
		}

		if (nbrInserts == 0) {
			return this.updateAll(handle, rows);
		}

		inserts = Arrays.copyOf(inserts, nbrInserts);
		updates = Arrays.copyOf(updates, nbrUpdates);
		final boolean insertOk = writeMany(handle, this.insertClause, this.insertParams, inserts);
		final boolean updateOk = writeMany(handle, this.updateClause, this.updateParams, updates);

		return insertOk && updateOk;
	}

	/**
	 * save all rows into the db. A row is inserted if it has no primary key,
	 * and is updated if it has primary key.
	 * NOTE: if any of the operation fails, we return a false. this may be used
	 * to roll-back the transaction.
	 *
	 * @param handle
	 *
	 * @param fieldValues
	 *            data to be saved
	 * @return true if it was indeed inserted or updated
	 * @throws SQLException
	 */
	boolean save(final DbHandle handle, final Object[] fieldValues) throws SQLException {
		if (this.generatedKeyIdx == -1) {
			final String msg = "Schema has no generated key. save opertion is not possible.";
			logger.error(msg);
			throw new SQLException(msg);
		}
		final Object key = fieldValues[this.generatedKeyIdx];
		if (key == null || ((Long) key) == 0L) {
			return this.insert(handle, fieldValues);
		}
		return this.update(handle, fieldValues);
	}

	private boolean updateOrInsert(final DbHandle handle, final Object[][] rows) throws SQLException {
		for (final Object[] row : rows) {
			final boolean updated = this.update(handle, row);
			if (updated) {
				continue;
			}

			final boolean inserted = this.insert(handle, row);
			if (!inserted) {
				logger.error(
						"Row failed to update or insert but with no error. Consdering this as business error, and raising an error flag and rolling back the transaction ");
				return false;
			}
		}
		return true;
	}

	/**
	 * insert all rows. NOTE: caller must consider rolling-back if false is
	 * returned
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if every one row was inserted. false if any one row failed
	 *         to insert.
	 * @throws SQLException
	 */
	boolean insertAll(final DbHandle handle, final Object[][] rows) throws SQLException {

		return writeMany(handle, this.insertClause, this.insertParams, rows);
	}

	/**
	 * update all rows. NOTE: caller must consider rolling-back if false is
	 * returned
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if every one row was successfully updated. false if any one
	 *         row failed to update
	 * @throws SQLException
	 */
	boolean updateAll(final DbHandle handle, final Object[][] rows) throws SQLException {

		return writeMany(handle, this.updateClause, this.updateParams, rows);
	}

	private static boolean writeMany(final DbHandle handle, final String sql, final FieldMetaData[] params,
			final Object[][] values) throws SQLException {

		final int nbrParams = params.length;
		final int nbrRows = values.length;
		/*
		 * create valueTypes array
		 */
		final ValueType[] types = new ValueType[nbrParams];
		final Object[][] rows = new Object[nbrRows][nbrParams];
		int idx = -1;
		for (final FieldMetaData p : params) {
			idx++;
			types[idx] = p.getValueType();
		}

		/*
		 * create a new list of array, based on the params. Note that a row in
		 * values[] is based on the fields in the schema, but we need the array
		 * based on the columns in the params. Hence we create a new list by
		 * copying values in te right order
		 *
		 */
		idx = -1;
		for (final Object[] target : rows) {
			idx++;
			final Object[] source = values[idx];

			int targetIdx = -1;
			for (final FieldMetaData p : params) {
				targetIdx++;
				target[targetIdx] = source[p.getIndex()];
			}
		}

		final int[] nbrs = handle.writeMany(sql, types, rows);

		/*
		 * we expect each element in nbrs to be 1.some times, rdbms returns -1
		 * stating that it is not sure, which means that the operation was
		 * actually successful. hence 0 means that the row failed to update
		 */
		idx = -1;
		boolean allOk = true;
		for (final int n : nbrs) {
			idx++;
			if (n == 0) {
				logger.error("Row at index {} failed to write to the data base", idx);
				allOk = false;
			}
		}
		return allOk;
	}

	private static String toMessage(final SQLException e, final String sql, final FieldMetaData[] params,
			final Object[] values) {
		final StringBuilder buf = new StringBuilder();
		buf.append("Sql Exception : ").append(e.getMessage());
		buf.append("SQL:").append(sql).append("\nParameters");
		final SQLException e1 = e.getNextException();
		if (e1 != null) {
			buf.append("\nLinked to the SqlExcpetion: ").append(e1.getMessage());
		}
		int idx = 1;
		for (final FieldMetaData p : params) {
			p.toMessage(buf, idx, values);
			idx++;
		}
		return buf.toString();
	}

	/**
	 * fetch data for this form from a db based on the primary key of this
	 * schema
	 *
	 * @param handle
	 * @param values
	 *            values array associated with this DataRow. in case this is
	 *            called from outside of a DataRow, then the length has to be
	 *            enough to extract values
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	boolean read(final DbHandle handle, final Object[] values) throws SQLException {
		if (values == null || values.length < this.nbrFieldsInARow) {
			logger.error(
					"This schema has {} fields but an array of length {} is assigned to receive data. Data not extracted.",
					this.nbrFieldsInARow, values == null ? 0 : values.length);
			return false;
		}

		final boolean[] result = new boolean[1];
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return Dba.this.selectClause + Dba.this.whereClause;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				int posn = 0;
				for (final FieldMetaData p : Dba.this.whereParams) {
					posn++;
					final Object value = p.setPsParam(ps, values, posn);
					if (value == null) {
						logger.error("fetch() invoked with key at index {} as null ", p.getIndex());
						throw new SQLException(
								"Primary key fields must be assigned values before a fetch() operations");
					}
				}
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				Dba.this.readWorker(rs, values);
				result[0] = true;
				/*
				 * return false to ask the driver to stop reading.
				 */
				return false;
			}
		});
		return result[0];
	}

	protected void readWorker(final ResultSet rs, final Object[] values) throws SQLException {
		int posn = 0;
		for (final FieldMetaData p : this.selectParams) {
			posn++;
			p.getFromRs(rs, posn, values);
		}
	}

	/**
	 * select multiple rows from the db based on the filtering criterion
	 *
	 * @param whereClauseStartingWithWhere
	 *            e.g. "WHERE a=? and b=?" null if all rows are to be read. Best
	 *            practice is to use parameters rather than dynamic sql. That is
	 *            you should use a=? rather than a = 32
	 * @param values
	 *            null or empty if where-clause is null or has no parameters.
	 *            every element MUST be non-null and must be one of the standard
	 *            objects we use String, Long, Double, Boolean, LocalDate,
	 *            Instant
	 * @param readOnlyOne
	 *            true if only the first row is to be read. false to read all
	 *            rows as per filtering criterion
	 *
	 * @param handle
	 * @return non-null, possibly empty array of rows
	 * @throws SQLException
	 */
	List<Object[]> filter(final String whereClauseStartingWithWhere, final Object[] values, final DbHandle handle)
			throws SQLException {
		final List<Object[]> result = new ArrayList<>();
		this.filterWorker(handle, whereClauseStartingWithWhere, values, null, result);

		return result;
	}

	boolean filterFirst(final String whereClauseStartingWithWhere, final Object[] inputValues,
			final Object[] outputValues, final DbHandle handle) throws SQLException {
		return this.filterWorker(handle, whereClauseStartingWithWhere, inputValues, outputValues, null);
	}

	boolean filterWorker(final DbHandle handle, final String where, final Object[] inputValues,
			final Object[] outputValues, final List<Object[]> outputRows) throws SQLException {

		final boolean result[] = new boolean[1];
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				if (where == null) {
					return Dba.this.selectClause;
				}
				return Dba.this.selectClause + ' ' + where;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				if (inputValues == null || inputValues.length == 0) {
					return;
				}
				int posn = 0;
				for (final Object value : inputValues) {
					posn++;
					ValueType.setObjectAsPsParam(value, ps, posn);
				}
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				/*
				 * receive data into a new row if this is for multiple rows
				 */
				Object[] vals = outputValues;
				if (vals == null) {
					vals = new Object[Dba.this.nbrFieldsInARow];
					outputRows.add(vals);
				}
				Dba.this.readWorker(rs, vals);
				result[0] = true;
				/*
				 * return false if we are to read just one row
				 */
				return outputValues == null;
			}
		});
		return result[0];
	}

	/**
	 * validate the data row for db-operation. This is to be invoked after the
	 * row is parsed/validated as a valid record (non-db)
	 *
	 * @param data
	 * @param rowNbr
	 * @param tableName
	 * @param ctx
	 * @param forInsert
	 * @return true if all ok. false if any error message is added to the
	 *         context
	 */
	public boolean validate(final Object[] data, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		boolean ok = true;
		for (final DbField field : this.dbFields) {
			if (field != null) {
				if (!field.validate(data, forInsert, ctx, tableName, rowNbr)) {
					ok = false;
				}
			}
		}
		return ok;
	}
}