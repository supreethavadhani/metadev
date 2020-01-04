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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * represents an RDBMS table/view
 *
 * @author simplity.org
 *
 */
public abstract class Schema {
	protected static final Logger logger = LoggerFactory.getLogger(Schema.class);
	/**
	 * name must be unique across tables and views
	 */
	protected String name;

	/**
	 * table/view name in the database
	 */
	protected String nameInDb;

	/*
	 * db operations that are to be exposed thru this form. array corresponds to
	 * the ordinals of IoType
	 */
	protected boolean[] operations;
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
	protected DbAssistant dbAssistant;

	/**
	 * @param data
	 * @return data table based on the data
	 */
	protected abstract SchemaDataTable newSchemaDataTable(Object[][] data);

	/**
	 * @return a new instance of data table based on this schema
	 */
	public abstract SchemaDataTable newSchemaDataTable();

	/**
	 *
	 * @return a db data that can carry data for this schema
	 */
	public abstract SchemaData newSchemaData();

	/**
	 *
	 * @param fieldValues
	 * @return a db data that can carry data for this schema
	 */
	protected abstract SchemaData newSchemaData(Object[] fieldValues);

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
	 *
	 * @return tenant field, or null if this schema does not define a tenant
	 *         field
	 */
	public DbField getTenantField() {
		if (this.dbAssistant == null) {
			return null;
		}
		return this.dbAssistant.tenantField;
	}

	/**
	 * create FieldMetaData for each of the indexed fields
	 *
	 * @param indexes
	 * @return
	 */
	protected FieldMetaData[] getParams(final int[] indexes) {
		final FieldMetaData[] result = new FieldMetaData[indexes.length];
		for (int i = 0; i < indexes.length; i++) {
			result[i] = new FieldMetaData(this.fields[indexes[i]]);
		}
		return result;
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
	 * @return the dbMetaData
	 */
	public DbAssistant getDbAssistant() {
		return this.dbAssistant;
	}

	/**
	 * parse the json into a row data
	 *
	 * @param array
	 * @param forInsert
	 * @param ctx
	 * @param tableName
	 *            name of this table. used for reporting errors
	 * @return non-null data row. but should not be used if errors are added to
	 *         the context
	 */
	public SchemaDataTable parseTable(final JsonArray array, final boolean forInsert, final IServiceContext ctx,
			final String tableName) {
		final List<Object[]> rows = new ArrayList<>();

		array.forEach(new Consumer<JsonElement>() {
			int idx = -1;

			@Override
			public void accept(final JsonElement ele) {
				this.idx++;
				if (ele.isJsonObject() == false) {
					logger.error("Json has a non-objetc element for table");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return;
				}
				final SchemaData dataRow = Schema.this.parseData((JsonObject) ele, forInsert, ctx, tableName, this.idx);
				rows.add(dataRow.fieldValues);
			}
		});
		return this.newSchemaDataTable(rows.toArray(new Object[0][]));
	}

	/**
	 * parse the json into a row data
	 *
	 * @param json
	 * @param forInsert
	 * @param ctx
	 * @param tableName
	 *            name of the child-table being parsed. null if this is teh main
	 *            form, and not a linked/child form
	 * @param rowNbr
	 *            0-based row number if this is a row in a child/linked table
	 * @return non-null data row. but should not be used if errors are added to
	 *         the context
	 */
	public SchemaData parseData(final JsonObject json, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {

		final SchemaData dataRow = this.newSchemaData();
		final Object[] row = dataRow.fieldValues;

		for (final Field field : this.fields) {
			final String value = getTextAttribute(json, field.getName());
			field.parseIntoRow(value, row, forInsert, ctx, tableName, rowNbr);
		}

		if (this.validations != null) {
			this.validateRow(dataRow, ctx);
		}

		return dataRow;
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param json
	 *            non-null
	 * @param ctx
	 *            can be null, if validations error need not be recorded into
	 *            the context. if non-null any validation error is added to it
	 * @return non-null data row into which keys are parsed. empty if no keys
	 *         are defined
	 *         in this schema. Errors if any,are added to the ctx
	 */
	public SchemaData parseKeys(final JsonObject json, final IServiceContext ctx) {
		final SchemaData dataRow = this.newSchemaData();
		final Object[] row = dataRow.getRawData();
		if (this.keyIndexes != null) {
			for (final int idx : this.keyIndexes) {
				final Field f = this.fields[idx];
				final String value = getTextAttribute(json, f.getName());
				validateAndSet(f, value, row, idx, false, ctx, null, 0);
			}
		}
		final Field field = this.getTenantField();

		if (field != null) {
			if (ctx == null) {
				logger.error(
						"Schema has tenant field, but no context isprovided to look it up. Tenant key not extracted to the data row");
			} else {
				row[field.index] = ctx.getTenantId();
			}
		}
		return dataRow;
	}

	private static String getTextAttribute(final JsonObject json, final String fieldName) {
		final JsonElement node = json.get(fieldName);
		if (node != null && node.isJsonPrimitive()) {
			return node.getAsString();
		}
		return null;
	}

	private static void validateAndSet(final Field field, final String value, final Object[] row, final int idx,
			final boolean forInsert, final IServiceContext ctx, final String tableName, final int rowNbr) {
		if (value == null || value.isEmpty()) {
			row[idx] = null;
			field.validateNull(forInsert, ctx, tableName, rowNbr);
			return;
		}
		row[idx] = field.parse(value, ctx, tableName, rowNbr);
	}

	private void validateRow(final SchemaData dataRow, final IServiceContext ctx) {
		for (final IValidation vln : this.validations) {
			final boolean ok = vln.isValid(dataRow, ctx);
			if (!ok) {
				logger.error("field {} failed an inter-field validaiton associated with it", vln.getFieldName());
			}
		}
	}

	/**
	 * only attr-value pairs are written object/array wrapper is to be managed
	 * by the caller
	 *
	 * @param row
	 * @param writer
	 * @throws IOException
	 */
	public void serializeToJson(final Object[] row, final JsonWriter writer) throws IOException {
		JsonUtil.writeFields(this.fields, row, writer);
	}

	/**
	 * @param data
	 *            row to be parsed/validated
	 * @param ctx
	 *            to which error are added to.
	 * @return data row. To be discarded if errors are added to ctx
	 */
	public SchemaData parseForInsert(final String[] data, final IServiceContext ctx) {
		final SchemaData dataRow = this.newSchemaData();
		final Object[] row = dataRow.fieldValues;

		for (final Field field : this.fields) {
			final String value = data[field.index];
			field.parseIntoRow(value, row, true, ctx, null, 0);
		}

		if (this.validations != null) {
			this.validateRow(dataRow, ctx);
		}

		return dataRow;
	}

	/**
	 * @return name of this schema
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * parse the input into a filter clause
	 *
	 * @param conditions
	 *            non-null {field1: {oper:"=", value:"abcd"...}
	 * @param sorts
	 *            how the result to be sorted {field1:"a/d",
	 * @param ctx
	 * @param maxRows
	 *            mxRows to be read
	 * @return filter clause that can be used to get rows from the db
	 */
	public ParsedFilter parseForFilter(final JsonObject conditions, final JsonObject sorts, final IServiceContext ctx,
			final int maxRows) {
		return ParsedFilter.parse(conditions, sorts, this.fieldsMap, this.getTenantField(), ctx, maxRows);
	}

	/**
	 * fetch data from the db.This method is suitable to be exposed as a service
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void read(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final SchemaData dataRow = this.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.read(handle);
			return true;
		}, true);

		if (result[0]) {
			try {
				dataRow.serializeAsJson(ctx.getResponseWriter());
			} catch (final IOException e) {
				final String msg = "I/O error while serializing e=" + e + ". message=" + e.getMessage();
				logger.error(msg);
				ctx.addMessage(Message.newError(msg));
			}
		} else {
			logger.error("No data found for the requested keys");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * update a row based on this schema. This method is used to expose a
	 * service
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void update(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final SchemaData dataRow = this.parseData(payload, false, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.update(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not updated, possibly because of time-stamp issues");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * service method to insert a row in the db
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void insert(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final SchemaData dataRow = this.parseData(payload, true, ctx, null, 0);
		if (!ctx.allOk()) {
			logger.error("Error while reading fields from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.insert(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not inserted, possibly because of issues with key");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * service method for filter operation
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void filter(final IServiceContext ctx, final JsonObject payload) throws Exception {
		JsonObject conditions = null;
		JsonElement node = payload.get(Conventions.Http.TAG_CONDITIONS);
		if (node != null && node.isJsonObject()) {
			conditions = (JsonObject) node;
		} else {
			logger.error("payload for filter has no conditions. ALl rows will be filtered");
		}

		/*
		 * sort order
		 */
		JsonObject sorts = null;
		node = payload.get(Conventions.Http.TAG_SORT);
		if (node != null && node.isJsonObject()) {
			sorts = (JsonObject) node;
		}

		int nbrRows = Conventions.Http.DEFAULT_NBR_ROWS;
		node = payload.get(Conventions.Http.TAG_MAX_ROWS);
		if (node != null && node.isJsonPrimitive()) {
			nbrRows = node.getAsInt();
		}

		final ParsedFilter filter = this.parseForFilter(conditions, sorts, ctx, nbrRows);

		if (!ctx.allOk()) {
			logger.warn("Filtering aborted due to errors in input data");
			return;
		}

		if (filter == null) {
			logger.error("DESIGN ERROR: form.parseForFilter() returned null, but failed to put ay error message. ");
			ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
			return;
		}

		final SchemaDataTable dataTable = this.newSchemaDataTable();
		RdbDriver.getDriver().transact(handle -> {
			dataTable.filter(handle, filter.getWhereClause(), filter.getWhereParamValues());
			return true;
		}, true);

		logger.info(" {} rows filtered", dataTable.length());

		try (JsonWriter writer = new JsonWriter(ctx.getResponseWriter())) {
			writer.beginObject();
			writer.name(Conventions.Http.TAG_LIST);
			dataTable.serializeAsJson(writer);
			writer.endObject();
		}
	}

	/**
	 * service method for delete operation
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void delete(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final SchemaData dataRow = this.parseKeys(payload, ctx);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}
		final boolean[] result = new boolean[1];

		RdbDriver.getDriver().transact(handle -> {
			result[0] = dataRow.delete(handle);
			return true;
		}, false);

		if (!result[0]) {
			logger.error("Row not deleted. Key issues?");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
		}

		return;
	}

	/**
	 * service method for bulk operation
	 *
	 * @param ctx
	 * @param payload
	 * @throws Exception
	 */
	public void bulkUpdate(final IServiceContext ctx, final JsonObject payload) throws Exception {
		final JsonArray arr = payload.getAsJsonArray(Conventions.Http.TAG_LIST);
		if (arr == null || arr.size() == 0) {
			logger.error("No data or data is empty");
			ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
			return;
		}

		final SchemaDataTable dataTable = this.parseTable(arr, true, ctx, null);
		if (!ctx.allOk()) {
			logger.error("Error while reading keys from the input payload");
			return;
		}

		RdbDriver.getDriver().transact(handle -> {
			final boolean ok = dataTable.save(handle);
			if (!ok) {
				logger.error("Error while saving rows into the DB. Operation abandoned and transaction is rolled back");
				ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
				return false;
			}
			logger.info("Data table saved all rows");
			return true;
		}, false);
	}

	/**
	 * get the service instance for the desired operation on this form
	 *
	 * @param opern
	 * @return service, or null if this form is not designed for this operation
	 */
	public IService getService(final IoType opern) {
		if (this.operations == null || this.operations[opern.ordinal()] == false) {
			return null;
		}

		switch (opern) {
		case Get:
			return new FormService(IoType.Get) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.read(ctx, inputPayload);

				}
			};

		case Filter:
			return new FormService(IoType.Filter) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.filter(ctx, inputPayload);

				}
			};

		case Create:
			return new FormService(IoType.Create) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.insert(ctx, inputPayload);

				}
			};

		case Update:
			return new FormService(IoType.Update) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.update(ctx, inputPayload);

				}
			};

		case Bulk:
			return new FormService(IoType.Bulk) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.bulkUpdate(ctx, inputPayload);

				}
			};

		case Delete:
			return new FormService(IoType.Delete) {

				@Override
				public void serve(final IServiceContext ctx, final JsonObject inputPayload) throws Exception {
					Schema.this.delete(ctx, inputPayload);

				}
			};

		default:
			logger.error("Form operation {} not yet implemented", opern);
			return null;
		}
	}

	protected abstract class FormService implements IService {
		protected final IoType opern;

		protected FormService(final IoType opern) {
			this.opern = opern;
		}

		@Override
		public String getId() {
			return this.opern.name() + '_' + Schema.this.name;
		}
	}
}
