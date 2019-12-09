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

package org.simplity.fm.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.form.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class Field {
	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	private static final String C = ", ";

	String name;
	String label;
	String altLabel;
	String placeHolder;
	String dataType;
	String errorId;
	String defaultValue;
	boolean isRequired;
	boolean isEditable;
	String listName;
	String listKey;
	String dbColumnName;
	ColumnType columnType;
	int index;

	void emitJavaCode(final StringBuilder sbf, final String dataTypesName) {
		sbf.append("\n\t\t\tnew Field(\"").append(this.name).append('"');
		sbf.append(C).append(this.index);
		sbf.append(C).append(dataTypesName).append('.').append(this.dataType);
		sbf.append(C).append(Util.escape(this.defaultValue));
		sbf.append(C).append(Util.escape(this.errorId));
		sbf.append(C).append(this.isRequired);
		sbf.append(C).append(this.isEditable);
		/*
		 * list is handled by inter-field in case key is specified
		 */
		if (this.listKey == null) {
			sbf.append(C).append(Util.escape(this.listName));
		} else {
			sbf.append(C).append("null");
		}
		sbf.append(C).append(Util.escape(this.dbColumnName));
		sbf.append(C);
		if (this.columnType == null) {
			sbf.append("null");
		} else {
			sbf.append("ColumnType.").append(this.columnType.name());
		}
		sbf.append(')');
	}

	void emitJavaCodeSimple(final StringBuilder sbf, final String dataTypesName) {
		sbf.append("\n\t\t\tnew Field(\"").append(this.name).append('"');
		sbf.append(C).append(this.index);
		sbf.append(C).append(dataTypesName).append('.').append(this.dataType);
		sbf.append(C).append(Util.escape(this.dbColumnName));
		sbf.append(C);
		if (this.columnType == null) {
			sbf.append("null");
		} else {
			sbf.append("ColumnType.").append(this.columnType.name());
		}
		sbf.append(')');
	}

	void emitFg(final StringBuilder sbf, final DataType dt) {
		if (dt == null) {
			final String msg = "Field " + this.name + " has an invalid data type of " + this.dataType
					+ ". Field not added.";
			logger.error(msg);
			sbf.append("\n\t//ERROR: ").append(msg);
			return;
		}
		/*
		 * validators
		 */
		final List<String> vals = new ArrayList<>();
		if (this.isRequired) {
			vals.add("Validators.required");
		}

		if (dt.name.equalsIgnoreCase("email")) {
			vals.add("Validators.email");
		} else {
			if (dt.valueType == ValueType.DECIMAL || dt.valueType == ValueType.INTEGER) {
				vals.add("Validators.max(" + dt.maxValue + ")");
				vals.add("Validators.min(" + dt.minValue + ")");
			}
			if (dt.regex != null && dt.regex.isEmpty() == false) {
				vals.add("Validators.pattern(" + Util.escapeTs(dt.regex) + ")");
			}
			if (this.isRequired && dt.minLength != 0) {
				vals.add("Validators.minLength(" + dt.minLength + ")");
			}
			if (dt.maxLength != 0) {
				vals.add("Validators.maxLength(" + dt.maxLength + ")");
			}
		}
		boolean isFirst = true;
		for (final String s : vals) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			sbf.append(s);
		}
	}

	void emitTs(final StringBuilder sbf, final DataType dt, final Map<String, ValueList> valueLists,
			final Map<String, KeyedList> keyedLists) {
		if (dt == null) {
			final String msg = "Field " + this.name + " has an invalid data type of " + this.dataType
					+ ". Field not added.";
			logger.error(msg);
			sbf.append("\n\t//ERROR: ").append(msg);
			return;
		}
		sbf.append("\n\t").append(this.name).append(": Field = {");

		sbf.append("\n\t\tname:").append(Util.escapeTs(this.name));
		emitAttr(sbf, "valueType", dt.valueType.ordinal());
		emitAttr(sbf, "defaultValue", this.defaultValue);
		emitAttr(sbf, "label", this.label);
		emitAttr(sbf, "altLabel", this.altLabel);
		emitAttr(sbf, "placeHolder", this.placeHolder);
		emitAttr(sbf, "trueLabel", dt.trueLabel);
		emitAttr(sbf, "falseLabel", dt.falseLabel);

		emitAttr(sbf, "isEditable", this.isEditable);
		String eid = this.errorId;
		if (eid == null || eid.isEmpty()) {
			eid = dt.errorId;
		}
		emitAttr(sbf, "errorId", eid);
		emitAttr(sbf, "isRequired", this.isRequired);
		emitAttr(sbf, "minLength", dt.minLength);
		emitAttr(sbf, "maxLength", dt.maxLength);
		emitAttr(sbf, "regex", dt.regex);
		emitAttr(sbf, "minValue", dt.minValue);
		emitAttr(sbf, "maxValue", dt.maxValue);
		emitAttr(sbf, "nbrFractions", dt.nbrFractions);
		emitAttr(sbf, "listName", this.listName);
		emitAttr(sbf, "listKey", this.listKey);
		if (this.listName != null) {
			if (this.listKey == null) {
				final ValueList list = valueLists.get(this.listName);
				if (list == null) {
					Form.logger.info("values not defined for {}. It is treated as a run-time list.", this.listName);
				} else {
					sbf.append("\n\t\t,valueList:[");
					list.emitTs(sbf, "\n\t\t\t\t");
					sbf.append("\n\t\t\t]");
				}
			} else {
				final KeyedList list = keyedLists.get(this.listName);
				if (list == null) {
					Form.logger.info("keyed-list of values not defined for {}. It is treated as a run-time list.",
							this.listName);
				} else {
					sbf.append("\n\t\t,keyedList:{");
					list.emitTs(sbf, "\n\t\t\t\t");
					sbf.append("\n\t\t\t}");
				}
			}
		}
		sbf.append("\n\t};");
	}

	private static void emitAttr(final StringBuilder sbf, final String attr, final Object value) {
		if (value == null) {
			return;
		}
		String s = value.toString();
		if (value instanceof String) {
			if (s.isEmpty()) {
				return;
			}
			s = Util.escapeTs(s);
		}
		sbf.append("\n\t\t,").append(attr).append(":").append(s);

	}
}
