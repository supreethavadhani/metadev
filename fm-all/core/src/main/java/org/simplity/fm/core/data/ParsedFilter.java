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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.FilterCondition;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility class used by dbRecord to parse input for a filter service
 *
 * @author simplity.org
 *
 */
class ParsedFilter {
	private static final Logger logger = LoggerFactory.getLogger(ParsedFilter.class);
	private static final String IN = " IN (";
	private static final String LIKE = " LIKE ? escape '\\'";
	private static final String BETWEEN = " BETWEEN ? and ?";
	private static final String WILD_CARD = "%";
	private static final String ESCAPED_WILD_CARD = "\\%";
	private static final String WILD_CHAR = "_";
	private static final String ESCAPED_WILD_CHAR = "\\_";

	final private String whereClause;
	final private Object[] whereParamValues;

	ParsedFilter(final String whereClauseStartingWithWhere, final Object[] whereParamValues) {
		this.whereClause = whereClauseStartingWithWhere;
		this.whereParamValues = whereParamValues;
	}

	String getWhereClause() {
		return this.whereClause;
	}

	Object[] getWhereParamValues() {
		return this.whereParamValues;
	}

	static ParsedFilter parse(final JsonObject json, final DbField[] fields, final DbField tenantField,
			final IServiceContext ctx) {
		JsonObject conditions = null;
		JsonElement node = json.get(Conventions.Http.TAG_CONDITIONS);
		if (node != null && node.isJsonObject()) {
			conditions = (JsonObject) node;
		} else {
			logger.warn("payload for filter has no conditions. ALl rows will be filtered");
		}

		/*
		 * sort order
		 */
		JsonObject sorts = null;
		node = json.get(Conventions.Http.TAG_SORT);
		if (node != null && node.isJsonObject()) {
			sorts = (JsonObject) node;
		}

		int nbrRows = Conventions.Http.DEFAULT_NBR_ROWS;
		node = json.get(Conventions.Http.TAG_MAX_ROWS);
		if (node != null && node.isJsonPrimitive()) {
			nbrRows = node.getAsInt();
		}

		logger.info("Number of max rows is set to {}. It is ignored as of now.", nbrRows);
		final StringBuilder sql = new StringBuilder();
		final List<Object> values = new ArrayList<>();

		/*
		 * force a condition on tenant id if required
		 */
		if (tenantField != null) {
			sql.append(tenantField.getColumnName()).append("=?");
			values.add(ctx.getTenantId());
		}

		final Map<String, DbField> map = new HashMap<>();
		for (final DbField field : fields) {
			map.put(field.getName(), field);
		}

		if (conditions != null && conditions.size() > 0) {
			final boolean ok = parseConditions(map, conditions, ctx, values, sql);
			if (!ok) {
				return null;
			}
		}

		if (sql.length() > 0) {
			sql.insert(0, " WHERE ");
		}

		if (sorts != null) {
			boolean isFirst = true;
			for (final Entry<String, JsonElement> entry : sorts.entrySet()) {
				final String f = entry.getKey();
				final DbField field = map.get(f);
				if (field == null) {
					logger.error("{} is not a field in the form. Sort order ignored");
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
		/*
		 * did we get anything at all?
		 */
		if (sql.length() == 0) {
			logger.info("Filter has no conditions or sort orders");
			return new ParsedFilter(null, null);
		}

		final String sqlText = sql.toString();
		logger.info("filter clause is: {}", sqlText);
		final int n = values.size();
		if (n == 0) {
			logger.info("Filter clause has no parametrs.");
			return new ParsedFilter(sqlText, null);
		}

		final StringBuilder sbf = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sbf.append('\n').append(i).append("= ").append(values.get(i));
		}
		logger.info("Filter parameters : {}", sbf.toString());
		return new ParsedFilter(sqlText, values.toArray(new Object[0]));
	}

	private static boolean parseConditions(final Map<String, DbField> fields, final JsonObject json,
			final IServiceContext ctx, final List<Object> values, final StringBuilder sql) {

		/*
		 * fairly long inside the loop for each field. But it is just
		 * serial code. Hence left it that way
		 */
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String fieldName = entry.getKey();
			final DbField field = fields.get(fieldName);
			if (field == null) {
				logger.warn("Input has value for a field named {} that is not part of this form", fieldName);
				continue;
			}

			final JsonElement node = entry.getValue();
			if (node == null || !node.isJsonObject()) {
				logger.error("Filter condition for field {} should be an object, but it is {}", fieldName, node);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			}

			final JsonObject con = (JsonObject) node;

			JsonElement ele = con.get(Conventions.Http.TAG_FILTER_COMP);
			if (ele == null || !ele.isJsonPrimitive()) {
				logger.error("comp is missing for a filter condition for field {}", fieldName);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			}
			final String condnText = ele.getAsString();
			final FilterCondition condn = FilterCondition.parse(condnText);
			if (condn == null) {
				logger.error("{} is not a valid filter condition", condnText);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			}

			ele = con.get(Conventions.Http.TAG_FILTER_VALUE);
			if (ele == null || !ele.isJsonPrimitive()) {
				logger.error("value is missing for a filter condition");
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			}
			String value = ele.getAsString();
			String value2 = null;
			if (condn == FilterCondition.Between) {
				ele = con.get(Conventions.Http.TAG_FILTER_VALUE_TO);
				if (ele == null || !ele.isJsonPrimitive()) {
					logger.error("valueTo is missing for a filter condition");
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}
				value2 = ele.getAsString();
			}

			final int idx = values.size();
			if (idx > 0) {
				sql.append(" and ");
			}

			sql.append(field.getColumnName());

			final ValueType vt = field.getValueType();
			Object obj = null;
			/*
			 * complex ones first.. we have to append ? to sql, and add type and
			 * value to the lists for each case
			 */
			if ((condn == FilterCondition.Contains || condn == FilterCondition.StartsWith)) {
				if (vt != ValueType.Text) {
					logger.error("Condition {} is not a valid for field {} which is of value type {}", condn, fieldName,
							vt);
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}

				sql.append(LIKE);
				value = escapeLike(value) + WILD_CARD;
				if (condn == FilterCondition.Contains) {
					value = WILD_CARD + value;
				}
				values.add(value);
				continue;
			}

			if (condn == FilterCondition.In) {
				sql.append(IN);
				boolean firstOne = true;
				for (final String part : value.split(",")) {
					obj = vt.parse(part.trim());
					if (obj == null) {
						logger.error("{} is not a valid value for value type {} for field {}", value, vt, fieldName);
						ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
						return false;
					}
					if (firstOne) {
						sql.append('?');
						firstOne = false;
					} else {
						sql.append(",?");
					}
					values.add(obj);
				}
				sql.append(')');
				continue;
			}

			obj = vt.parse(value);
			if (obj == null) {
				logger.error("{} is not a valid value for value type {} for field {}", value, vt, fieldName);
				ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
				return false;
			}

			if (condn == FilterCondition.Between) {
				Object obj2 = null;
				if (value2 != null) {
					obj2 = vt.parse(value2);
				}
				if (obj2 == null) {
					logger.error("{} is not a valid value for value type {} for field {}", value2, vt, fieldName);
					ctx.addMessage(Message.newError(Message.MSG_INVALID_DATA));
					return false;
				}
				sql.append(BETWEEN);
				values.add(obj);
				values.add(obj2);
				continue;
			}

			sql.append(' ').append(condnText).append(" ?");
			values.add(obj);
		}
		return true;

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
