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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Represents field values in an entity. Like an EntityBean/DTO/DAO all rolled
 * into one. This is one of the core classes
 * </p>
 * <p>
 * An entity consists of fields that carry values. We have chosen an array of
 * objects as the data-structure for field values. Of course, the fields are not
 * in any order, but we have have chosen an array over map for ease of access
 * with a generated meta-data for fields that include their index in the array
 * </p>
 *
 * <p>
 * While such an approach is quite useful for the framework to carry out its job
 * of auto-plumbing data between the client and the DB,it is quite painful for a
 * programmer to write custom code around such an API that requires array index.
 * For example setLongValue(int, long) is error prone, even if we provide static
 * literals for the index (like Customer.ID).Hence
 * Hence we also provide the standard getters and setters. These are generated
 * from the schema/form
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class SchemaData extends ValueObject {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaData.class);
	/**
	 * schema for which this data is created
	 */
	protected final Schema schema;

	protected SchemaData(final Schema schema, final Object[] values) {
		super(schema.fields, values);
		this.schema = schema;
	}

	/**
	 * insert/create this form data into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	public boolean insert(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.insert(handle, this.fieldValues);
	}

	/**
	 * update this form data back into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.update(handle, this.fieldValues);
	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean delete(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.delete(handle, this.fieldValues);
	}

	/**
	 * fetch data for this form from a db
	 *
	 * @param handle
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean read(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.read(handle, this.fieldValues);
	}

	/**
	 * @param handle
	 * @param whereClauseStartingWithWhere
	 *            e.g. "WHERE a=? and b=?" null if all rows are to be read. Best
	 *            practice is to use parameters rather than dynamic sql. That is
	 *            you should use a=? rather than a = 32
	 * @param values
	 *            null or empty if where-clause is null or has no parameters.
	 *            every element MUST be non-null and must be one of the standard
	 *            objects we use String, Long, Double, Boolean, LocalDate,
	 *            Instant
	 * @return true if a row was read into this object. false otherwise
	 * @throws SQLException
	 */
	public boolean filterFirstOne(final DbHandle handle, final String whereClauseStartingWithWhere,
			final Object[] values) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.filterFirst(whereClauseStartingWithWhere, values, this.fieldValues, handle);
	}

	/**
	 * insert or update this, based on the primary key. possible only if the
	 * primary key is generated
	 *
	 * @param handle
	 * @return true if it was indeed saved
	 * @throws SQLException
	 */
	public boolean save(final DbHandle handle) throws SQLException {
		final DbAssistant asst = this.schema.getDbAssistant();
		if (asst == null) {
			this.noOps();
			return false;
		}
		return asst.save(handle, this.fieldValues);
	}

	private void noOps() {
		logger.error("Form {} is not designed for db operation", this.schema.name);
	}

}