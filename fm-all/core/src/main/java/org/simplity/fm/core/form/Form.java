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
package org.simplity.fm.core.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.FilterCondition;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class Form {
	private static final Logger logger = LoggerFactory.getLogger(Form.class);
	private static final String IN = " IN (";
	private static final String LIKE = " LIKE ? escape '\\'";
	private static final String BETWEEN = " BETWEEN ? and ?";
	private static final String WILD_CARD = "%";
	private static final String ESCAPED_WILD_CARD = "\\%";
	private static final String WILD_CHAR = "_";
	private static final String ESCAPED_WILD_CHAR = "\\_";
	/**
	 * this is the unique id of this form. It is the section name in case it is
	 * a section of a composite form
	 */
	protected String uniqueName;
	/**
	 * forms maybe revised periodically, and we may have to keep tarck of them
	 */
	protected String version;
	/**
	 * Fields in this form.
	 */
	protected Field[] fields;

	/**
	 * field name that has the user id. Used for access control
	 */
	protected String userIdFieldName;

	/**
	 * name of grids/tabular dta. null if there are no grids. grid name is the
	 * "fieldName" used in this form for the tabular data. like orderLines.;
	 */
	protected ChildForm[] childForms;

	/**
	 * describes all the inter-field validations, and form-level validations
	 */
	protected IValidation[] validations;


	/**
	 * index to the values array for the key fields. this is derived based on
	 * fields. This is based on the field meta data attribute isKeyField
	 */
	protected int[] keyIndexes;
	/**
	 * index to the values array for the fields that make-up unique key.
	 */
	protected int[] uniqueIndexes;
	/**
	 * fields are also stored as Maps for ease of access
	 */
	protected Map<String, Field> fieldMap;
	/**
	 * indexes of tabular fields are also stored in map for ease of access
	 */
	protected Map<String, ChildForm> childMap;

	/**
	 * index to the field that represents the userId. User access may be
	 * implemented based on this field
	 */
	protected int userIdFieldIdx = -1;

	/**
	 * meta data required for db operations. null if this is not designed for db
	 * operations
	 */
	protected DbMetaData dbMetaData;

	/**
	 * MUST BE CALLED after setting all protected fields
	 */
	protected void initialize() {
		if (this.fields != null) {
			int n = this.fields.length;
			this.fieldMap = new HashMap<>(n, 1);
			int[] keys = new int[n];
			int[] uniqs = new int[n];
			int keyIdx = 0;
			int uniqIdx = 0;
			for (Field field : this.fields) {
				this.fieldMap.put(field.getFieldName(), field);
				if (field.isKeyField()) {
					keys[keyIdx] = field.getIndex();
					keyIdx++;
				}
				if (field.getColumnType() == ColumnType.UniqueKey) {
					uniqs[uniqIdx] = field.getIndex();
					uniqIdx++;
				}
			}
			if (keyIdx != 0) {
				this.keyIndexes = Arrays.copyOf(keys, keyIdx);
			}
			if (uniqIdx != 0) {
				this.uniqueIndexes = Arrays.copyOf(uniqs, uniqIdx);
			}
		}

		if (this.childForms != null) {
			int n = this.childForms.length;
			this.childMap = new HashMap<>(n, 1);
			for (ChildForm child : this.childForms) {
				this.childMap.put(child.fieldName, child);
			}
		}

		if (this.userIdFieldName != null) {
			Field field = this.fieldMap.get(this.userIdFieldName);
			if (field == null) {
				logger.error("userIdField {} specified, but not defined", this.userIdFieldName);
			} else {
				this.userIdFieldIdx = field.getIndex();
			}
		}
		/*
		 * db meta for child forms?
		 */
		if (this.dbMetaData != null && this.dbMetaData.dbLinks != null) {
			this.initializeDbLinks();
		}
	}

	private static final String WH = " WHERE ";

	/**
	 * 
	 */
	private void initializeDbLinks() {
		boolean foundOne = false;
		StringBuilder sbf = new StringBuilder(WH);
		for (int i = 0; i < this.childForms.length; i++) {
			DbLink link = this.dbMetaData.dbLinks[i];
			Form childForm = this.childForms[i].form;
			if (link == null) {
				logger.info("Form {} has a child {} but has no childMeta entry for it", this.getFormId(),
						childForm.getFormId());
				continue;
			}
			Form form = this.childForms[i].form;
			if (form.dbMetaData == null) {
				logger.warn("Child {} has no db meta data. It will not particiapte in db I/O of its parent",
						childForm.getFormId());
				continue;
			}

			link.childMeta = form.dbMetaData;
			foundOne = true;
			/*
			 * reset the sbf to re-use it
			 */
			sbf.setLength(WH.length());
			for (String f : link.childLinkNames) {
				Field field = form.getField(f);
				if (field == null) {
					logger.error(
							"Child link field {} is specified in parent form, but is not defiined as a field in the child form {}",
							f, form.getFormId());
					foundOne = false;
					break;
				}
				sbf.append(field.getDbColumnName()).append("=?, ");
			}
			/*
			 * remove the last comma..
			 */
			sbf.setLength(sbf.length() - 2);
			link.linkWhereClause = sbf.toString();
			link.nbrChildFields = form.fields.length;
		}

		if (!foundOne) {
			this.dbMetaData.dbLinks = null;
		}
	}

	/**
	 * @return the userIdFieldIdx. -1 if user id field is not present in this
	 *         form
	 */
	public int getUserIdFieldIdx() {
		return this.userIdFieldIdx;
	}

	/**
	 * unique id assigned to this form. like customerDetails. This is unique
	 * across all types of forms within a project
	 * 
	 * @return non-null unique id
	 */
	public String getFormId() {
		if (this.version == null) {
			return this.uniqueName;
		}
		return this.uniqueName + '_' + this.version;
	}

	/**
	 * @return the keyIndexes
	 */
	public int[] getKeyIndexes() {
		return this.keyIndexes;
	}

	/**
	 * @return the keyIndexes
	 */
	public int[] getUniqIndexes() {
		return this.uniqueIndexes;
	}

	/**
	 * @return the fieldNames. non-null. could be empty
	 */
	public Field[] getFields() {
		return this.fields;
	}

	/**
	 * @return the grid names. non-null. could be empty
	 */
	public ChildForm[] getChildForms() {
		return this.childForms;
	}

	/**
	 * @param fieldName
	 * @return data element or null if there is no such field
	 */
	public Field getField(String fieldName) {
		return this.fieldMap.get(fieldName);
	}

	/**
	 * 
	 * @param childName
	 * @return child-form structure that represents the sub-form in this form
	 */
	public ChildForm getChildForm(String childName) {
		return this.childMap.get(childName);
	}

	/**
	 * @return the validations
	 */
	public IValidation[] getValidations() {
		return this.validations;
	}

	/**
	 * 
	 * @return db meta data for this form
	 */
	public DbMetaData getDbMetaData() {
		return this.dbMetaData;
	}

	protected FormDbParam[] getParams(int[] indexes) {
		FormDbParam[] params = new FormDbParam[indexes.length];
		for (int i = 0; i < params.length; i++) {
			int idx = indexes[i];
			params[i] = new FormDbParam(idx, this.fields[idx].getValueType());
		}
		return params;
	}

	/**
	 * 
	 * @param rows
	 * @return child data for this form based on rows of data
	 */
	public FormData[] createChildData(Object[][] rows) {
		FormData[] result = new FormData[rows.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new FormData(this, rows[i], null);
		}
		return result;
	}

	protected DbLink newDbLink(String[] names, int[] indexes) {
		DbLink c = new DbLink();
		c.linkParentParams = this.getParams(indexes);
		c.childLinkNames = names;
		return c;
	}

	/**
	 * @return a data structure
	 */
	public FormData newFormData() {
		Object[] row = new Object[this.fields.length];
		for (Field field : this.fields) {
			Object val = field.getDefaultValue();
			if (val != null) {
				row[field.getIndex()] = val;
			}
		}
		return new FormData(this, row, null);
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
	public SqlReader parseForFilter(JsonObject conditions, JsonObject sorts, List<Message> errors, IServiceContext ctx,
			int maxRows) {
		StringBuilder sql = new StringBuilder(this.dbMetaData.selectClause);
		sql.append(" WHERE ");
		List<FormDbParam> params = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		/*
		 * force a condition on tenant id id required
		 */
		Field tenant = this.dbMetaData.tenantField;
		if (tenant != null) {
			sql.append(tenant.getDbColumnName()).append("=?");
			values.add(ctx.getTenantId());
			params.add(new FormDbParam(0, tenant.getValueType()));
		}

		/*
		 * fairly long inside the loop for each field. But it is more
		 * serial code. Hence left it that way
		 */
		for (Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
			String fieldName = entry.getKey();
			Field field = this.getField(fieldName);
			if (field == null) {
				logger.warn("Input has value for a field named {} that is not part of this form", fieldName);
				continue;
			}

			JsonElement node = entry.getValue();
			if (node == null || !node.isJsonObject()) {
				logger.error("Filter condition for filed {} should be an object, but it is {}", fieldName, node);
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}

			JsonObject con = (JsonObject) node;

			JsonElement ele = con.get(Conventions.Http.TAG_FILTER_COMP);
			if (ele == null || !ele.isJsonPrimitive()) {
				logger.error("comp is missing for a filter condition");
				errors.add(Message.newError(Message.MSG_INVALID_DATA));
				return null;
			}
			String condnText = ele.getAsString();
			FilterCondition condn = FilterCondition.parse(condnText);
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

			int idx = params.size();
			if (idx > 0) {
				sql.append(" and ");
			}

			sql.append(field.getDbColumnName());
			ValueType vt = field.getValueType();
			Object obj = null;
			logger.info("Found a condition : field {} {} {} . value2={}",field.getFieldName(), condn.name(), value, value2);
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
				params.add(new FormDbParam(idx++, vt));
				value = escapeLike(value);
				if (condn == FilterCondition.Contains) {
					values.add(WILD_CARD + value + WILD_CARD);
				} else {
					values.add(WILD_CARD + value);
				}
				continue;
			}

			if (condn == FilterCondition.In) {
				sql.append(IN);
				boolean firstOne = true;
				for (String part : value.split(",")) {
					obj = vt.parse(part.trim());
					if (value == null) {
						logger.error("{} is not a valid value for value type {} for field {}", value, vt, fieldName);
						errors.add(Message.newError(Message.MSG_INVALID_DATA));
						return null;
					}
					params.add(new FormDbParam(idx++, vt));
					values.add(obj);
					if (firstOne) {
						sql.append('?');
						firstOne = false;
					} else {
						sql.append(",?");
					}
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
				values.add(obj);
				params.add(new FormDbParam(idx++, vt));
				values.add(obj2);
				params.add(new FormDbParam(idx++, vt));
				continue;
			}

			sql.append(' ').append(condnText).append(" ?");
			params.add(new FormDbParam(idx++, vt));
			values.add(obj);
		}

		if (sorts != null) {
			boolean isFirst = true;
			for (Entry<String, JsonElement> entry : sorts.entrySet()) {
				String f = entry.getKey();
				Field field = this.fieldMap.get(f);
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
				sql.append(field.getDbColumnName());
				if (entry.getValue().getAsString().toLowerCase().startsWith("d")) {
					sql.append(" DESC ");
				}
			}
		}
		String sqlText = sql.toString();
		logger.info("Filter sql = {}", sqlText);
		return new SqlReader(sql.toString(), params.toArray(new FormDbParam[0]), values.toArray(new Object[0]));
	}

	/**
	 * NOTE: Does not work for MS-ACCESS. but we are fine with that!!!
	 * 
	 * @param string
	 * @return string that is escaped for a LIKE sql operation.
	 */
	private static String escapeLike(String string) {
		return string.replaceAll(WILD_CARD, ESCAPED_WILD_CARD).replaceAll(WILD_CHAR, ESCAPED_WILD_CHAR);
	}

	/**
	 * @return number of fields in this form
	 */
	public int getNbrFields() {
		return this.fields.length;
	}
}
