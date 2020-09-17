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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class Field implements Util.INamedMember {
	private static final Map<String, FieldType> fieldTypes = createMap();
	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	protected static final String C = ", ";

	String name;
	String dataType;
	String errorId;
	String defaultValue;
	boolean isRequired;
	String listName;
	String listKey;
	String label;
	String icon;
	String fieldSuffix;
	String fieldPrefix;
	String placeHolder;
	String hint;
	boolean renderInList;
	boolean renderInSave;
	ValueType valueType;

	int index;
	String dbColumnName;
	private String fieldType;

	@Override
	public void setNameAndIdx(final String name, final int idx) {
		this.name = name;
		this.index = idx;
	}

	void init(final Map<String, DataType> dataTypes) {
		final DataType dt = dataTypes.get(this.dataType);
		if (dt == null) {
			logger.error("Field {} has an invalid dataType of {}. text value type is assumed", this.name,
					this.dataType);
			this.valueType = ValueType.Text;
		} else {
			this.valueType = dt.valueType;

		}
	}

	void emitJavaCode(final StringBuilder sbf, final String dataTypesName, final boolean isDb) {
		sbf.append("\n\t\t\tnew ");
		if (isDb) {
			sbf.append("Db");
		}
		sbf.append("Field(\"").append(this.name).append('"');
		sbf.append(C).append(this.index);
		sbf.append(C).append(dataTypesName).append('.').append(this.dataType);
		sbf.append(C).append(Util.escape(this.defaultValue));
		sbf.append(C).append(Util.escape(this.errorId));
		/*
		 * list is handled by inter-field in case key is specified
		 */
		if (this.listKey == null) {
			sbf.append(C).append(Util.escape(this.listName));
		} else {
			sbf.append(C).append("null");
		}
		if (isDb) {
			sbf.append(C).append(Util.escape(this.dbColumnName));
			sbf.append(C);
			final FieldType ct = this.getFieldType();
			if (ct == null) {
				sbf.append("null");
			} else {
				sbf.append("FieldType.").append(ct.name());
			}

		} else {
			sbf.append(C).append(this.isRequired);
		}
		sbf.append(')');
	}

	protected void emitTs(final StringBuilder def, final StringBuilder controls, final Map<String, DataType> dataTypes,
			final String prefix, final Map<String, ValueList> lists, final Map<String, KeyedList> keyedLists) {
		final List<String> validations = new ArrayList<>();

		if (this.isRequired) {
			def.append(prefix).append("isRequired: true");
			validations.add("required");
		}

		if (this.listName != null) {
			def.append(prefix).append("listName: ").append(Util.escapeTs(this.listName));

			if (this.listKey != null) {
				def.append(prefix).append("listKey: ").append(Util.escapeTs(this.listKey));
				final KeyedList kl = keyedLists.get(this.listName);
				if (kl != null) {
					def.append(prefix).append("keyedList: {");
					final String indent = "\n\t\t\t";
					kl.emitTs(def, indent);
					def.append(indent).append("}");
				}
			} else {
				final ValueList list = lists.get(this.listName);
				if (list != null) {
					final String indent = "\n\t\t\t";
					def.append(prefix).append("valueList: [");
					list.emitTs(def, indent);
					def.append(indent).append("]");

				}
			}
		}

		final DataType dt = dataTypes.get(this.dataType);
		if (dt == null) {
			def.append(prefix).append("valueType: 0");
			logger.error("Field {} has an invalid data type of {}", this.name, this.dataType);
		} else {
			dt.emitTs(def, this.defaultValue, validations, prefix);
		}

		controls.append("\n\t\tthis.controls.set('").append(this.name).append("', [");
		boolean firstOne = true;
		for (final String s : validations) {
			if (firstOne) {
				firstOne = false;
			} else {
				controls.append(", ");
			}
			controls.append("Validators.").append(s);
		}

		controls.append("]);");
		controls.append("\n\t\tthis.fields.set('").append(this.name).append("', this.").append(this.name).append(");");
	}

	/**
	 * @return column type, or null.
	 */
	public FieldType getFieldType() {
		if (this.fieldType == null) {
			return FieldType.OptionalData;
		}
		final FieldType ct = fieldTypes.get(this.fieldType.toLowerCase());
		if (ct != null) {
			return ct;
		}
		logger.error("{} is an invalid fieldType for field {}. optional data is  assumed", this.fieldType, this.name);
		return FieldType.OptionalData;
	}

	/**
	 * @return
	 */
	private static Map<String, FieldType> createMap() {
		final Map<String, FieldType> map = new HashMap<>();
		for (final FieldType vt : FieldType.values()) {
			map.put(vt.name().toLowerCase(), vt);
		}
		return map;
	}

	static final String BEGIN = "\n\t\t\t";
	static final String END = "',";
	static final char COMA = ',';

	/**
	 * @param sbf
	 */
	public void emitFormTs(final StringBuilder sbf) {
		sbf.append("\n\t\t").append(this.name).append(": {");
		sbf.append(BEGIN).append("name: '").append(this.name).append(END);
		sbf.append(BEGIN).append("dataType: '").append(this.dataType).append(END);
		sbf.append(BEGIN).append("isRequired: ").append(this.isRequired).append(COMA);
		String lbl = this.label;
		if (lbl == null || lbl.isEmpty()) {
			lbl = Util.toClassName(this.name);
		}
		Util.addAttr(sbf, BEGIN, "label", lbl);
		Util.addAttr(sbf, BEGIN, "defaultValue", this.defaultValue);
		Util.addAttr(sbf, BEGIN, "icon", this.icon);
		Util.addAttr(sbf, BEGIN, "suffix", this.fieldSuffix);
		Util.addAttr(sbf, BEGIN, "prefix", this.fieldPrefix);
		Util.addAttr(sbf, BEGIN, "placeHolder", this.placeHolder);
		Util.addAttr(sbf, BEGIN, "hint", this.hint);
		Util.addAttr(sbf, BEGIN, "errorId", this.errorId);
		Util.addAttr(sbf, BEGIN, "listName", this.listName);
		Util.addAttr(sbf, BEGIN, "listKeyName", this.listKey);
		sbf.append("\n\t\t}");
	}
}
