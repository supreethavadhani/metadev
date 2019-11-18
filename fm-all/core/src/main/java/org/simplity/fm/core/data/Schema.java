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
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.FilterCondition;
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
public class Schema {
	protected static final Logger logger = LoggerFactory.getLogger(Schema.class);
	private static final String IN = " IN (";
	private static final String LIKE = " LIKE ? escape '\\'";
	private static final String BETWEEN = " BETWEEN ? and ?";
	private static final String WILD_CARD = "%";
	private static final String ESCAPED_WILD_CARD = "\\%";
	private static final String WILD_CHAR = "_";
	private static final String ESCAPED_WILD_CHAR = "\\_";
	/**
	 * name must be unique across tables and views
	 */
	protected String name;

	/**
	 * table/view name in the database
	 */
	protected String nameInDb;

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
	protected DbMetaData dbMetaData;

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
	 * create FieldMetaData for each of the indexed fields
	 *
	 * @param indexes
	 * @return
	 */
	protected FieldMetaData[] getParams(final int[] indexes) {
		final FieldMetaData[] result = new FieldMetaData[indexes.length];
		int idx = -1;
		for (final int i : indexes) {
			idx++;
			result[idx] = new FieldMetaData(this.fields[i]);
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
	 *
	 * @return a db data that can carry data for this schema
	 */
	public DataRow newDataRow() {
		return new DataRow(this);
	}

	/**
	 * @return the dbMetaData
	 */
	public DbMetaData getDbMetaData() {
		return this.dbMetaData;
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
	public DataTable parseTable(final JsonArray array, final boolean forInsert, final IServiceContext ctx,
			final String tableName) {
		final List<Object[]> rows = new ArrayList<>();

		array.forEach(new Consumer<JsonElement>() {
			int idx = -1;

			@Override
			public void accept(final JsonElement ele) {
				this.idx++;
				if (ele.isJsonArray() == false) {
					logger.error("Json has a non-objetc element for table");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return;
				}
				final DataRow dataRow = Schema.this.parseRow((JsonObject) ele, forInsert, ctx, tableName, this.idx);
				rows.add(dataRow.dataRow);
			}
		});
		return new DataTable(this, rows.toArray(new Object[0][]));
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
	public DataRow parseRow(final JsonObject json, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {

		final DataRow dataRow = new DataRow(this);
		final Object[] row = dataRow.dataRow;

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
	public DataRow parseKeys(final JsonObject json, final IServiceContext ctx) {
		final DataRow dataRow = new DataRow(this);
		final Object[] row = dataRow.getDataRow();
		if (this.keyIndexes != null) {
			for (final int idx : this.keyIndexes) {
				final Field f = this.fields[idx];
				final String value = getTextAttribute(json, f.getName());
				validateAndSet(f, value, row, idx, false, ctx, null, 0);
			}
		}
		final Field field = this.dbMetaData.tenantField;
		if (field != null) {
			row[field.index] = ctx.getTenantId();
		}
		return dataRow;
	}

	private static String getTextAttribute(final JsonObject json, final String fieldName) {
		final JsonElement node = json.get(fieldName);
		if (node == null) {
			return null;
		}
		if (node.isJsonPrimitive()) {
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

	private void validateRow(final DataRow dataRow, final IServiceContext ctx) {
		for (final IValidation vln : this.validations) {
			final boolean ok = vln.isValid(dataRow, ctx);
			if (!ok) {
				logger.error("field {} failed an inter-field validaiton associated with it", vln.getFieldName());
			}
		}
	}

	/**
	 * only attr-value pairs are written object/array wrapper is to be manged by
	 * the caller
	 *
	 * @param row
	 * @throws IOException
	 */
	void serializeToJson(final Object[] row, final JsonWriter writer) throws IOException {
		for (final Field field : this.fields) {
			writer.name(field.name);
			final Object value = row[field.index];
			if (value == null) {
				writer.nullValue();
				return;
			}
			final ValueType vt = field.getValueType();
			if (vt == ValueType.INTEGER || vt == ValueType.DECIMAL) {
				writer.value((Number) (value));
				return;
			}
			if (vt == ValueType.BOOLEAN) {
				writer.value((boolean) (value));
				return;
			}
			writer.value(value.toString());
		}

	}

	/**
	 * @param data
	 *            row to be parsed/validated
	 * @param ctx
	 *            to which error are added to.
	 * @return data row. To be discarded if errors are added to ctx
	 */
	public DataRow parseForInsert(final String[] data, final IServiceContext ctx) {
		final DataRow dataRow = new DataRow(this);
		final Object[] row = dataRow.dataRow;

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
	 * @param errors
	 * @param ctx
	 * @param maxRows
	 *            mxRows to be read
	 * @return filter clause that can be used to get rows from the db
	 */
	public FilterSql parseForFilter(final JsonObject conditions, final JsonObject sorts, final List<Message> errors,
			final IServiceContext ctx, final int maxRows) {
		final StringBuilder sql = new StringBuilder(this.dbMetaData.selectClause);
		sql.append(" WHERE ");
		final List<PreparedStatementParam> params = new ArrayList<>();

		/*
		 * force a condition on tenant id id required
		 */
		final Field tenant = this.dbMetaData.tenantField;
		if (tenant != null) {
			sql.append(tenant.getColumnName()).append("=?");
			params.add(new PreparedStatementParam(ctx.getTenantId(), tenant.getValueType()));
		}

		/*
		 * fairly long inside the loop for each field. But it is more
		 * serial code. Hence left it that way
		 */
		for (final Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
			final String fieldName = entry.getKey();
			final DbField field = this.getField(fieldName);
			if (field == null) {
				logger.warn("Input has value for a field named {} that is not part of this form", fieldName);
				continue;
			}

			final JsonElement node = entry.getValue();
			if (node == null || !node.isJsonObject()) {
				logger.error("Filter condition for filed {} should be an object, but it is {}", fieldName, node);
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}

			final JsonObject con = (JsonObject) node;

			JsonElement ele = con.get(Conventions.Http.TAG_FILTER_COMP);
			if (ele == null || !ele.isJsonPrimitive()) {
				logger.error("comp is missing for a filter condition");
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}
			final String condnText = ele.getAsString();
			final FilterCondition condn = FilterCondition.parse(condnText);
			if (condn == null) {
				logger.error("{} is not a valid filter condition", condnText);
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}

			ele = con.get(Conventions.Http.TAG_FILTER_VALUE);
			if (ele == null || !ele.isJsonPrimitive()) {
				logger.error("value is missing for a filter condition");
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}
			String value = ele.getAsString();
			String value2 = null;
			if (condn == FilterCondition.Between) {
				ele = con.get(Conventions.Http.TAG_FILTER_VALUE_TO);
				if (ele == null || !ele.isJsonPrimitive()) {
					logger.error("valueTo is missing for a filter condition");
					errors.add(Message.newError(Message.MSG_INVALID_DATA));
					return null;
				}
				value2 = ele.getAsString();
			}

			final int idx = params.size();
			if (idx > 0) {
				sql.append(" and ");
			}

			sql.append(field.getColumnName());
			final ValueType vt = field.getValueType();
			Object obj = null;
			logger.info("Found a condition : field {} {} {} . value2={}", field.getName(), condn.name(), value, value2);
			/*
			 * complex ones first.. we have to append ? to sql, and add type and
			 * value to the lists for each case
			 */
			if ((condn == FilterCondition.Contains || condn == FilterCondition.StartsWith)) {
				if (vt != ValueType.TEXT) {
					logger.error("Condition {} is not a valid for field {} which is of value type {}", condn, fieldName,
							vt);
					errors.add(Message.newError(Message.MSG_INVALID_DATA));
					return null;
				}

				sql.append(LIKE);
				value = WILD_CARD + escapeLike(value);
				if (condn == FilterCondition.Contains) {
					value += WILD_CARD;
				}
				params.add(new PreparedStatementParam(value, vt));
				continue;
			}

			if (condn == FilterCondition.In) {
				sql.append(IN);
				boolean firstOne = true;
				for (final String part : value.split(",")) {
					obj = vt.parse(part.trim());
					if (value == null) {
						logger.error("{} is not a valid value for value type {} for field {}", value, vt, fieldName);
						errors.add(Message.newError(Message.MSG_INVALID_DATA));
						return null;
					}
					if (firstOne) {
						sql.append('?');
						firstOne = false;
					} else {
						sql.append(",?");
					}
					params.add(new PreparedStatementParam(obj, vt));
				}
				sql.append(')');
				continue;
			}

			obj = vt.parse(value);
			if (value == null) {
				logger.error("{} is not a valid value for value type {} for field {}", value, vt, fieldName);
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}

			if (condn == FilterCondition.Between) {
				Object obj2 = null;
				if (value2 != null) {
					obj2 = vt.parse(value2);
				}
				if (obj2 == null) {
					logger.error("{} is not a valid value for value type {} for field {}", value2, vt, fieldName);
					errors.add(Message.newError(Message.MSG_INVALID_DATA));
					return null;
				}
				sql.append(BETWEEN);
				params.add(new PreparedStatementParam(obj, vt));
				params.add(new PreparedStatementParam(obj2, vt));
				continue;
			}

			sql.append(' ').append(condnText).append(" ?");
			params.add(new PreparedStatementParam(obj, vt));
		}

		if (sorts != null) {
			boolean isFirst = true;
			for (final Entry<String, JsonElement> entry : sorts.entrySet()) {
				final String f = entry.getKey();
				final Field field = this.fieldsMap.get(f);
				if (field == null) {
					logger.error("{} is not a field in teh form. Sort order ignored");
					continue;
				}
				if (isFirst) {
					sql.append(" ORDER BY ");
					isFirst = false;
				} else {
					sql.append(", ");
				}
				sql.append(field.getColumnName());
				if (entry.getValue().getAsString().toLowerCase().startsWith("d")) {
					sql.append(" DESC ");
				}
			}
		}
		final String sqlText = sql.toString();
		logger.info("Filter sql = {}", sqlText);
		return new FilterSql(sql.toString(), params.toArray(new PreparedStatementParam[0]));
	}

	/**
	 * NOTE: Does not work for MS-ACCESS. but we are fine with that!!!
	 *
	 * @param string
	 * @return string that is escaped for a LIKE sql operation.
	 */
	private static String escapeLike(final String string) {
		return string.replaceAll(WILD_CARD, ESCAPED_WILD_CARD).replaceAll(WILD_CHAR, ESCAPED_WILD_CHAR);
	}
}
