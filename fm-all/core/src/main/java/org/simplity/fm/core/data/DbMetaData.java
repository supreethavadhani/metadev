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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DbMetaData {
	protected static final Logger logger = LoggerFactory.getLogger(DbMetaData.class);
	/**
	 * e.g. where a=? and b=?
	 */
	protected final String whereClause;
	/**
	 * db parameters to be used for the where clause
	 */
	protected final FieldMetaData[] whereParams;
	/**
	 * e.g. select a,b,c from t
	 */
	protected final String selectClause;
	/**
	 * db parameters to be used to receive data from the result set of the
	 * select query
	 */
	protected final FieldMetaData[] selectParams;
	/**
	 * e.g insert a,b,c,d into table1 values(?,?,?,?)
	 */
	protected final String insertClause;
	/**
	 * db parameters for the insert sql
	 */
	protected final FieldMetaData[] insertParams;

	/**
	 * e.g. update table1 set a=?, b=?, c=?
	 */
	protected final String updateClause;
	/**
	 * db parameters for the update sql
	 */
	protected final FieldMetaData[] updateParams;

	/**
	 * e.g. delete from table1. Note that where is not part of this.
	 */
	protected final String deleteClause;

	/**
	 * db column name that is generated as internal key. null if this is not
	 * relevant
	 */
	protected final String generatedColumnName;

	/**
	 *
	 */
	protected final int generatedKeyIdx;

	/**
	 * if this APP is designed for multi-tenant deployment, and this table has
	 * data across tenants..
	 */
	protected final Field tenantField;

	/**
	 * if this table allows update, and needs to use time-stamp-match technique
	 * to avoid concurrent updates..
	 */
	protected final Field timestampField;

	/**
	 * number of fields in the schema to which this meta data is attached
	 */
	protected final int nbrFieldsInARow;

	/**
	 * constructor when there is no key. only filter will be allowed
	 *
	 * @param nbrFieldsInARow
	 * @param tenantField
	 * @param selectClause
	 * @param selectParams
	 */
	public DbMetaData(final int nbrFieldsInARow, final Field tenantField, final String selectClause,
			final FieldMetaData[] selectParams) {
		this.nbrFieldsInARow = nbrFieldsInARow;
		this.tenantField = tenantField;
		this.selectClause = selectClause;
		this.selectParams = selectParams;
		this.whereClause = null;
		this.whereParams = null;
		this.insertClause = null;
		this.insertParams = null;
		this.updateClause = null;
		this.updateParams = null;
		this.deleteClause = null;
		this.generatedColumnName = null;
		this.generatedKeyIdx = -1;
		this.timestampField = null;
	}

	/**
	 *
	 * @param nbrFieldsInARow
	 * @param tenantField
	 * @param selectClause
	 * @param selectParams
	 * @param whereClause
	 * @param whereParams
	 * @param insertClause
	 * @param insertParams
	 * @param updateClause
	 * @param updateParams
	 * @param deleteClause
	 * @param generatedColumnName
	 * @param generatedKeyIdx
	 * @param timestampField
	 */
	public DbMetaData(final int nbrFieldsInARow, final Field tenantField, final String selectClause,
			final FieldMetaData[] selectParams, final String whereClause, final FieldMetaData[] whereParams,
			final String insertClause, final FieldMetaData[] insertParams, final String updateClause,
			final FieldMetaData[] updateParams, final String deleteClause, final String generatedColumnName,
			final int generatedKeyIdx, final Field timestampField) {
		this.whereClause = whereClause;
		this.whereParams = whereParams;
		this.selectClause = selectClause;
		this.selectParams = selectParams;
		this.insertClause = insertClause;
		this.insertParams = insertParams;
		this.updateClause = updateClause;
		this.updateParams = updateParams;
		this.deleteClause = deleteClause;
		this.generatedColumnName = generatedColumnName;
		this.generatedKeyIdx = generatedKeyIdx;
		this.tenantField = tenantField;
		this.timestampField = timestampField;
		this.nbrFieldsInARow = nbrFieldsInARow;
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
	public boolean insert(final DbHandle handle, final Object[] values) throws SQLException {
		int n = 0;
		if (this.generatedColumnName != null) {
			try {
				final long[] generatedKeys = new long[1];
				n = handle.insertAndGenerteKeys(new IDbWriter() {

					@Override
					public String getPreparedStatement() {
						return DbMetaData.this.insertClause;
					}

					@Override
					public boolean setParams(final PreparedStatement ps) throws SQLException {
						int posn = 1;
						final StringBuilder sbf = new StringBuilder("Parameter Values");
						for (final FieldMetaData p : DbMetaData.this.insertParams) {
							final Object value = values[p.idx];
							p.valueType.setPsParam(ps, posn, value);
							sbf.append('\n').append(posn).append('=').append(value);
							posn++;
						}
						logger.info(sbf.toString());
						return true;
					}

				}, this.generatedColumnName, generatedKeys);
				final long id = generatedKeys[0];
				if (id == 0) {
					logger.error("DB handler did not return generated key");
				} else {
					values[this.generatedKeyIdx] = generatedKeys[0];
					logger.info("Generated key {] assigned back to form data", id);
				}
			} catch (final SQLException e) {
				final String msg = toMessage(e, this.insertClause, this.insertParams, values);
				logger.error(msg);
				throw new SQLException(msg, e);
			}
		} else {
			n = writeWorker(handle, this.insertClause, this.insertParams, values);
		}
		if (n == 0) {
			return false;
		}
		return true;
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
	public boolean update(final DbHandle handle, final Object[] values) throws SQLException {
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
	public boolean deleteFromDb(final DbHandle handle, final Object[] values) throws SQLException {
		final String sql = this.deleteClause + this.whereClause;
		final int nbr = writeWorker(handle, sql, this.whereParams, values);
		return nbr > 0;
	}

	private static int writeWorker(final DbHandle handle, final String sql, final FieldMetaData[] params,
			final Object[] values) throws SQLException {
		try {
			final int n = handle.write(new IDbWriter() {

				@Override
				public String getPreparedStatement() {
					return sql;
				}

				@Override
				public boolean setParams(final PreparedStatement ps) throws SQLException {
					int posn = 1;
					final StringBuilder sbf = new StringBuilder("Parameter Values");
					for (final FieldMetaData p : params) {
						final Object value = values[p.idx];
						p.valueType.setPsParam(ps, posn, value);
						sbf.append('\n').append(posn).append('=').append(value);
						posn++;
					}
					logger.info(sbf.toString());
					return true;
				}

			});
			return n;
		} catch (final SQLException e) {
			final String msg = toMessage(e, sql, params, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
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
	 * @return true if all ok. false in case of any problem, caller shoudl
	 *         roll-back if this is false
	 * @throws SQLException
	 */
	public boolean saveAll(final DbHandle handle, final Object[][] rows) throws SQLException {
		if (this.generatedKeyIdx == -1) {
			logger.error(
					"This schema has no generated key, and hence meta-data can not determine whether to insert or update a row.");
			return false;
		}
		final int nbrRows = rows.length;
		/*
		 * we create array with max length rather than list
		 */
		final Object[][] inserts = new Object[nbrRows][];
		final Object[][] updates = new Object[nbrRows][];
		int nbrInserts = 0;
		int nbrUpdates = 0;

		for (final Object[] row : rows) {
			if (row[this.generatedKeyIdx] == null) {
				inserts[nbrInserts] = row;
				nbrInserts++;
			} else {
				updates[nbrUpdates] = row;
				nbrUpdates++;
			}
		}

		if (nbrUpdates == 0) {
			return this.insertAll(handle, rows);
		}

		if (nbrInserts == 0) {
			return this.updateAll(handle, rows);
		}

		final boolean insertOk = this.insertAll(handle, Arrays.copyOf(inserts, nbrInserts));
		final boolean updateOk = this.updateAll(handle, Arrays.copyOf(updates, nbrUpdates));
		return insertOk && updateOk;
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
	public boolean insertAll(final DbHandle handle, final Object[][] rows) throws SQLException {

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
	public boolean updateAll(final DbHandle handle, final Object[][] rows) throws SQLException {

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
			types[idx] = p.valueType;
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
				target[targetIdx] = source[p.idx];
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
				logger.error("Row at index {} failed to write to eh data base");
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
	 * @param values
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean fetch(final DbHandle handle, final Object[] values) throws SQLException {
		final boolean[] result = new boolean[1];
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return DbMetaData.this.selectClause + DbMetaData.this.whereClause;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				int posn = 1;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FieldMetaData p : DbMetaData.this.whereParams) {
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
				for (final FieldMetaData p : DbMetaData.this.selectParams) {
					values[p.getIndex()] = p.getValueType().getFromRs(rs, posn);
					posn++;
				}
				result[0] = true;
				return false;
			}
		});
		return result[0];
	}

	/**
	 * select multiple rows from the db based on the filtering criterion
	 *
	 * @param handle
	 * @param filterWhereClause
	 *            like "WHERE a=? and b=?"
	 * @param whereValues
	 *            one for each parameter in the where clause
	 * @return non-null, possibly empty array of rows
	 * @throws SQLException
	 */
	public Object[][] filter(final DbHandle handle, final String filterWhereClause,
			final PreparedStatementParam[] whereValues) throws SQLException {
		final List<Object[]> result = new ArrayList<>();
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return DbMetaData.this.selectClause + filterWhereClause;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				int posn = 0;
				for (final PreparedStatementParam p : whereValues) {
					posn++;
					p.valueType.setPsParam(ps, posn, p.value);
				}
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				final Object[] row = new Object[DbMetaData.this.nbrFieldsInARow];
				result.add(row);
				int posn = 0;
				for (final FieldMetaData p : DbMetaData.this.selectParams) {
					posn++;
					row[p.idx] = p.valueType.getFromRs(rs, posn);
				}
				return true;
			}
		});
		return result.toArray(new Object[0][]);
	}
}
