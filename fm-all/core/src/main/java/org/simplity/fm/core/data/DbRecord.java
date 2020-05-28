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

import java.sql.SQLException;
import java.util.List;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.rdb.RecordProcessor;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Extends a record to add functionality to link the underlying data-structure
 * to a database/persistence
 * </p>
 * <p>
 * concrete classed must add named getters and setters to the record so that the
 * user-code is type-safe
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class DbRecord extends Record {
	protected static final Logger logger = LoggerFactory.getLogger(DbRecord.class);

	protected final Dba dba;

	protected DbRecord(final Dba dba, final RecordMetaData meta, final Object[] fieldValues) {
		super(meta, fieldValues);
		this.dba = dba;
	}

	@Override
	public boolean parse(final IInputObject inputObject, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		if (!super.parse(inputObject, forInsert, ctx, tableName, rowNbr)) {
			return false;
		}
		/*
		 * validate db-specific fields
		 */
		return this.dba.validate(this.fieldValues, forInsert, ctx, tableName, rowNbr);
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param inputObject
	 *            non-null
	 * @param ctx
	 *            non-null. any validation error is added to it
	 * @return true if all ok. false if any parse error is added the ctx
	 */
	public boolean parseKeys(final IInputObject inputObject, final IServiceContext ctx) {
		return this.dba.parseKeys(inputObject, this.fieldValues, ctx);
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
		return this.dba.read(handle, this.fieldValues);
	}

	/**
	 * read is expected to succeed. hence an exception is thrown in case if no
	 * row is not read
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void readOrFail(final DbHandle handle) throws SQLException {
		if (!this.dba.read(handle, this.fieldValues)) {
			throw new SQLException("Read failed for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	/**
	 * select multiple rows from the db based on the filtering criterion. Meant
	 * for use by frame-work and utilities. End programmers should not use this
	 * API. They should use a filter-sql instead.
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
	 *
	 * @param handle
	 * @return non-null, possibly empty array of rows
	 * @throws SQLException
	 */
	public List<Object[]> filter(final String whereClauseStartingWithWhere, final Object[] values,
			final DbHandle handle) throws SQLException {
		return this.dba.filter(whereClauseStartingWithWhere, values, handle);
	}

	/**
	 * use filter-criterion, but only one row is expected, and hence read it
	 * into this record.
	 * This API is meant for utility programs,and not for end-programmers.
	 * filter-sqls are a better choice for end-programmers as they provide
	 * type-safe way to set/get values
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
	 * @param handle
	 * @return true if the first row is read. false otherwise
	 * @throws SQLException
	 */
	public boolean filterFirst(final String whereClauseStartingWithWhere, final Object[] values, final DbHandle handle)
			throws SQLException {
		return this.dba.filterFirst(whereClauseStartingWithWhere, values, this.fieldValues, handle);
	}

	/**
	 * use filter-criterion, but only one row is expected, and hence read it
	 * into this record.
	 *
	 * This API is meant for utility programs,and not for end-programmers.
	 * filter-sqls are a better choice for end-programmers as they provide
	 * type-safe way to set/get values
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
	 * @param handle
	 * @throws SQLException
	 */
	public void filterFirstOrFail(final String whereClauseStartingWithWhere, final Object[] values,
			final DbHandle handle) throws SQLException {
		if (this.dba.filterFirst(whereClauseStartingWithWhere, values, this.fieldValues, handle)) {
			return;
		}

		throw new SQLException("Filter operation is expected to get one row, but none turned-up!");
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
		return this.dba.insert(handle, this.fieldValues);
	}

	/**
	 * insert is expected to succeed. hence an exception is thrown in case if no
	 * row is not inserted
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void insertOrFail(final DbHandle handle) throws SQLException {
		if (!this.dba.insert(handle, this.fieldValues)) {
			throw new SQLException(
					"Insert failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
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
		return this.dba.update(handle, this.fieldValues);
	}

	/**
	 * update is expected to succeed. hence an exception is thrown in case if no
	 * row is updated
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void updateOrFail(final DbHandle handle) throws SQLException {
		if (!this.dba.update(handle, this.fieldValues)) {
			throw new SQLException(
					"Update failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
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
		return this.dba.save(handle, this.fieldValues);
	}

	/**
	 * @param handle
	 * @throws SQLException
	 */
	public void saveOrFail(final DbHandle handle) throws SQLException {
		if (!this.dba.save(handle, this.fieldValues)) {
			throw new SQLException(
					"Save failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}

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
		return this.dba.delete(handle, this.fieldValues);
	}

	/**
	 * delete is expected to succeed. hence an exception is thrown in case if no
	 * row is not deleted
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void deleteOrFail(final DbHandle handle) throws SQLException {
		if (!this.dba.delete(handle, this.fieldValues)) {
			throw new SQLException(
					"Delete failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	@Override
	public DbRecord makeACopy() {
		return this.newInstance(this.fieldValues);
	}

	@Override
	public DbRecord newInstance() {
		return this.newInstance(null);
	}

	@Override
	public abstract DbRecord newInstance(Object[] values);

	/**
	 * get a service for the specific operation on this record
	 *
	 * @param operation
	 *            non-null
	 * @param serviceName
	 *            optional. If not specified, record-names service name is used
	 * @return service if this record is designed for this operation. null
	 *         otherwise.
	 */
	public IService getService(final IoType operation, final String serviceName) {
		if (!this.dba.operationAllowed(operation)) {
			logger.info("{} operation is not allowed on record {}", operation, this.fetchName());
			return null;
		}

		String sn = serviceName;
		if (sn == null || sn.isEmpty()) {
			sn = operation.name();
			sn = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + '_' + this.fetchName();
		}
		switch (operation) {
		case Get:
			return new Reader(sn);
		case Create:
			return new Creater(sn);
		case Update:
			return new Updater(sn);
		case Delete:
			return new Deleter(sn);
		case Filter:
			return new Filter(sn);
		default:
			throw new ApplicationError("DbRecord needs to be designed for operation " + operation.name());
		}
	}

	protected abstract class Service implements IService {
		private final String serviceName;

		protected Service(final String name) {
			this.serviceName = name;
		}

		@Override
		public String getId() {
			return this.serviceName;
		}
	}

	protected class Reader extends Service {

		protected Reader(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while reading keys from the input payload");
				return;
			}

			RdbDriver.getDriver().read(handle -> {
				if (!rec.read(handle)) {
					logger.error("No data found for the requested keys");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				}
			});

			if (ctx.allOk()) {
				ctx.setAsResponse(rec);
			}
		}
	}

	protected class Creater extends Service {

		protected Creater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parse(payload, true, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}

			RdbDriver.getDriver().readWrite(handle -> {
				if (rec.insert(handle)) {
					return true;
				}

				logger.error("Insert operation failed silently");
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			});
		}
	}

	protected class Updater extends Service {

		protected Updater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parse(payload, false, ctx, null, 0)) {
				logger.error("Error while validating data from the input payload");
				return;
			}

			RdbDriver.getDriver().readWrite(handle -> {
				if (rec.update(handle)) {
					return true;
				}

				logger.error("Update operation failed silently");
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			});
		}
	}

	protected class Deleter extends Service {

		protected Deleter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while validating keys");
				return;
			}

			RdbDriver.getDriver().readWrite(handle -> {
				if (rec.delete(handle)) {
					return true;
				}

				logger.error("Delete operation failed silently");
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			});

		}
	}

	protected class Filter extends Service {

		protected Filter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputObject payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			final ParsedFilter filter = rec.dba.parseFilter(payload, ctx);
			if (!ctx.allOk()) {
				logger.error("Error while parsing filter conditions from th einput payload");
				return;
			}
			final Object[][][] result = new Object[1][][];
			RdbDriver.getDriver().read(handle -> {
				final List<Object[]> list = rec.dba.filter(filter.getWhereClause(), filter.getWhereParamValues(),
						handle);
				if (list.size() == 0) {
					logger.warn("No rows filtered. Responding with empty list");
				}
				result[0] = list.toArray(new Object[0][]);
			});

			ctx.setAsResponse(rec.fetchFields(), result[0]);
		}

	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated
	 * classes
	 * 
	 * @param fieldName
	 * @return db field specified by this name, or null if there is no such name
	 */
	public DbField fetchField(final String fieldName) {
		return this.dba.getField(fieldName);
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated
	 * classes
	 *
	 * @return index of the generated key, or -1 if this record has no generated
	 *         key
	 */
	public int fetchGeneratedKeyIndex() {
		return this.dba.getGeneratedKeyIndex();
	}

	/**
	 * API meant for utility programs. End-programmers should use filter-sql
	 * instead
	 *
	 * @param whereClause
	 * @param values
	 * @param handle
	 * @param rowProcessor
	 * @throws SQLException
	 */
	public void forEach(final String whereClause, final Object[] values, final DbHandle handle,
			final RecordProcessor rowProcessor) throws SQLException {

		this.dba.forEach(handle, whereClause, values, row -> {
			final DbRecord rec = DbRecord.this.newInstance(row);
			return rowProcessor.process(rec);
		});
	}
}
