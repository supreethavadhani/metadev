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
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

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

	/**
	 * dbRecord has one peculiar issue with generated primary key. it is
	 * optional for insert operation but is mandatory for update. To take care
	 * of this, we need a parameter to indicate the purpose of parsing. Also, we
	 * need to populate some fields
	 *
	 * @param json
	 *            input data
	 * @param forInsert
	 *            true if the data is being parsed for an insert operation,
	 *            false if it is meant for an update instead
	 * @param ctx
	 * @param tableName
	 *            if the input data is for a table.collection if this record,
	 *            then this is the name of the attribute with which the table is
	 *            received. null if the data is at the root level, else n
	 * @param rowNbr
	 *            relevant if tablaeName is not-null.
	 * @return true if all ok. false if any error message is added to the
	 *         context
	 */
	public boolean parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		/*
		 * parse it as a non-db record first..
		 */
		if (!super.parse(json, ctx, tableName, rowNbr)) {
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
	 * @param json
	 *            non-null
	 * @param ctx
	 *            non-null. any validation error is added to it
	 * @return true if all ok. false if any parse error is added the ctx
	 */
	public boolean parseKeys(final JsonObject json, final IServiceContext ctx) {
		return this.dba.parseKeys(json, this.fieldValues, ctx);
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
			throw new SQLException("Read failed for " + this.getName() + this.dba.emitKeys(this.fieldValues));
		}
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
					"Insert failed silently for " + this.getName() + this.dba.emitKeys(this.fieldValues));
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
					"Update failed silently for " + this.getName() + this.dba.emitKeys(this.fieldValues));
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
					"Delete failed silently for " + this.getName() + this.dba.emitKeys(this.fieldValues));
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
	protected abstract DbRecord newInstance(Object[] values);

	/**
	 * get a service for the specific operation on this record
	 *
	 * @param operation
	 *            non-null
	 * @return service if this record is designed for this operation. null
	 *         otherwise.
	 */
	public IService getService(final IoType operation) {
		if (!this.dba.operationAllowed(operation)) {
			logger.info("{} operation is not allowed on record {}", operation, this.getName());
		}

		String serviceName = operation.name();
		serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + '_' + this.getName();
		switch (operation) {
		case Get:
			return new Reader(serviceName);
		case Create:
			return new Creater(serviceName);
		case Update:
			return new Updater(serviceName);
		case Delete:
			return new Deleter(serviceName);
		case Filter:
			return new Filter(serviceName);
		default:
			throw new ApplicationError("DbRecord is needs to be designed for operation " + operation.name());
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
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
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
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
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
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
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
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
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
		public void serve(final IServiceContext ctx, final JsonObject payload) throws Exception {
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

			ctx.setAsResponse(rec.getFields(), result[0]);
		}

	}

	/**
	 * @param fieldName
	 * @return db field specified by this name, or null if there is no such name
	 */
	public DbField getField(final String fieldName) {
		return this.dba.getField(fieldName);
	}
}
