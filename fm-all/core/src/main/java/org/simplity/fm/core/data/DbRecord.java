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
	/**
	 * db operations that are to be exposed thru this form. array corresponds to
	 * the ordinal of IoType
	 */
	protected boolean[] operations;

	protected Dba dba;

	protected DbRecord(final Field[] fields, final Object[] fieldValues) {
		super(fields, fieldValues);
	}

	/**
	 * parsing data for a DbREcord requires the purpose why it is done.
	 * Specifically, if it is meant for insert or update.
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
	 *            received. null if the data is at te root level, else n
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
		return true;
	}

	/**
	 * set parameter values to a prepared statement that uses this Vo as input
	 * source.
	 *
	 * @param ps
	 * @throws SQLException
	 */
	public void setPsParams(final PreparedStatement ps) throws SQLException {
		int idx = 0;
		for (final Field field : this.fields) {
			final Object value = this.fieldValues[idx];
			idx++;
			field.getValueType().setPsParam(ps, idx, value);
		}
	}

	/**
	 * read values from a result set for which this record is designed as output
	 * data structure
	 *
	 * @param rs
	 * @throws SQLException
	 */
	public void readFromRs(final ResultSet rs) throws SQLException {
		int idx = 0;
		for (final Field field : this.fields) {
			this.fieldValues[idx] = field.getValueType().getFromRs(rs, idx + 1);
			idx++;
		}
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
		if (this.operations[IoType.Get.ordinal()]) {
			return this.dba.read(handle, this.fieldValues);
		}

		return this.notAllowed(IoType.Get);
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
		if (!this.operations[IoType.Get.ordinal()]) {
			throw new SQLException("Read not allowed on " + this.getName());
		}

		if (!this.dba.insert(handle, this.fieldValues)) {
			throw new SQLException("Read failed for " + this.getName() + this.emitKeys());
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
		if (this.operations[IoType.Create.ordinal()]) {
			return this.dba.insert(handle, this.fieldValues);
		}
		return this.notAllowed(IoType.Create);
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
		if (!this.operations[IoType.Create.ordinal()]) {
			throw new SQLException("Insert not allowed on schema " + this.getName());
		}
		if (!this.dba.insert(handle, this.fieldValues)) {
			throw new SQLException("Insert failed silently for " + this.getName() + this.emitKeys());
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
		if (this.operations[IoType.Update.ordinal()]) {
			return this.dba.update(handle, this.fieldValues);
		}
		return this.notAllowed(IoType.Update);
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
		if (!this.operations[IoType.Update.ordinal()]) {
			throw new SQLException("Update not allowed on schema " + this.getName());
		}
		if (!this.dba.update(handle, this.fieldValues)) {
			throw new SQLException("Update failed silently for " + this.getName() + this.emitKeys());
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
		if (this.operations[IoType.Update.ordinal()] && this.operations[IoType.Create.ordinal()]) {
			return this.dba.save(handle, this.fieldValues);
		}
		return this.notAllowed(IoType.Update);
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
		if (this.operations[IoType.Delete.ordinal()]) {
			return this.dba.delete(handle, this.fieldValues);
		}
		return this.notAllowed(IoType.Delete);
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
		if (!this.operations[IoType.Delete.ordinal()]) {
			throw new SQLException("Delete not allowed on schema " + this.getName());
		}
		if (!this.dba.delete(handle, this.fieldValues)) {
			throw new SQLException("Delete failed silently for " + this.getName() + this.emitKeys());
		}
	}

	private String emitKeys() {
		final int[] ids = this.dba.keyIndexes;
		if (ids == null) {
			return "No keys";
		}
		final StringBuilder sbf = new StringBuilder();
		for (final int idx : ids) {
			sbf.append(this.fields[idx].getName()).append(" = ").append(this.fieldValues[idx]).append("  ");
		}
		return sbf.toString();
	}

	private boolean notAllowed(final IoType operation) {
		logger.error("Form {} is not designed for '{}' operation", this.getName(), operation);
		return false;
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
		if (!this.operations[operation.ordinal()]) {
			logger.info("{} operation is not allowed on record {}", operation, this.getName());
		}

		String name = operation.name();
		name = name.substring(0, 1).toLowerCase() + name.substring(1) + '_' + this.getName();
		switch (operation) {
		case Get:
			return new Reader(name);
		case Create:
			return new Creater(name);
		case Update:
			return new Updater(name);
		case Delete:
			return new Deleter(name);
		case Filter:
			return new Filter(name);
		default:
			throw new ApplicationError("DbRecord is needs to be designed for operation " + operation.name());
		}
	}

	protected abstract class Service implements IService {
		private final String name;

		protected Service(final String name) {
			this.name = name;
		}

		@Override
		public String getId() {
			return this.name;
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
			final ParsedFilter filter = ParsedFilter.parse(payload, rec.dba.dbFields, rec.dba.tenantField, ctx);
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

			ctx.setAsResponse(rec.fields, result[0]);
		}

	}
}
