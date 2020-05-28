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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Form {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);

	private static final String C = ", ";

	String name;
	String recordName;
	String[] operations;
	Control[] controls;
	LinkedForm[] linkedForms;
	// Section[] sections;

	/*
	 * derived attributes
	 */
	Map<String, Field> fields;
	Record record;

	final Set<String> keyFieldNames = new HashSet<>();

	Field[] keyFields;

	void initialize(final Record rec) {
		this.fields = new HashMap<>();

		this.record = rec;
		for (final Field f : rec.fieldMap.values()) {
			final FieldType ct = f.getFieldType();
			if (ct == FieldType.PrimaryKey || ct == FieldType.GeneratedPrimaryKey) {
				this.keyFieldNames.add(f.name);
			}
		}

		this.fields.putAll(rec.fieldMap);

		if (this.linkedForms != null) {
			int idx = 0;
			for (final LinkedForm lf : this.linkedForms) {
				lf.index = idx;
				idx++;
			}
		}
	}

	void emitJavaForm(final StringBuilder sbf, final String packageName) {
		/*
		 * our package name is rootPackage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");
		Util.emitImport(sbf, ComponentProvider.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.LinkMetaData.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.LinkedForm.class);
		final String recordClass = Util.toClassName(this.recordName) + "Record";
		sbf.append("\nimport ").append(packageName).append(".rec.").append(recordClass).append(';');

		final String cls = Util.toClassName(this.name) + "Form";
		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form ").append(this.name).append("  */\npublic class ");
		sbf.append(cls).append(" extends Form<").append(recordClass).append("> {");

		final String p = "\n\tprotected static final ";

		/*
		 * protected static final Field[] FIELDS = {.....};
		 */
		sbf.append(p).append("String NAME = \"").append(this.name).append("\";");
		/*
		 * protected static final String RECORD = "....";
		 */
		sbf.append(p).append(recordClass).append(" RECORD = (").append(recordClass);
		sbf.append(") ComponentProvider.getProvider().getRecord(\"").append(this.recordName).append("\");");

		/*
		 * protected static final boolean[] OPS = {true, false,..};
		 */
		sbf.append(p);
		getOps(this.operations, sbf);

		/*
		 * linked forms
		 */
		final String lf = "\n\tprivate static final LinkedForm<?>[] LINKS = ";
		if (this.linkedForms == null) {
			sbf.append(lf).append("null;");
		} else {
			final StringBuilder bf = new StringBuilder();
			for (int i = 0; i < this.linkedForms.length; i++) {
				/*
				 * declare linkedMeta and Form
				 */
				this.linkedForms[i].emitJavaCode(sbf, this.fields, i);

				if (i != 0) {
					bf.append(',');
				}
				bf.append("new LinkedForm(L").append(i).append(", F").append(i).append(')');
			}
			sbf.append(lf).append('{').append(bf).append("};");
		}

		/*
		 * constructor
		 *
		 */
		sbf.append("\n/** constructor */\npublic ").append(cls).append("() {");
		sbf.append("\n\t\tsuper(NAME, RECORD, OPS, LINKS);\n\t}");

		sbf.append("\n}\n");
	}

	private static final Map<String, Integer> OP_INDEXES = getOpIndexes();

	static void getOps(final String[] dbOps, final StringBuilder sbf) {
		final IoType[] types = IoType.values();
		final boolean[] ops = new boolean[types.length];
		if (dbOps != null) {

			for (final String op : dbOps) {
				final Integer idx = OP_INDEXES.get(op.toLowerCase());
				if (idx == null) {
					logger.error("{} is not a valid db operation (IoType). Ignored.");
				} else {
					ops[idx] = true;
				}
			}
		}
		sbf.append(" boolean[] OPS = {");
		boolean firstOne = true;
		for (final boolean b : ops) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(", ");
			}
			sbf.append(b);
		}
		sbf.append("};");
	}

	/**
	 * @return
	 */
	private static Map<String, Integer> getOpIndexes() {
		final Map<String, Integer> indexes = new HashMap<>();
		for (final IoType iot : IoType.values()) {
			indexes.put(iot.name().toLowerCase(), iot.ordinal());
		}
		return indexes;
	}

	/**
	 * @param sbf
	 * @param typesMap
	 * @param lists
	 * @param keyedLists
	 * @param tsImportPrefix
	 */
	void emitTs(final StringBuilder sbf, final Map<String, DataType> dataTypes, final Map<String, ValueList> lists,
			final Map<String, KeyedList> keyedLists, final String tsImportPrefix) {
		sbf.append("\nimport { Form , Field, ChildForm } from '").append(tsImportPrefix).append("form';");
		sbf.append("\nimport { FormData } from '").append(tsImportPrefix).append("formData';");
		sbf.append("\nimport { SelectOption, Vo } from '").append(tsImportPrefix).append("types';");
		sbf.append("\nimport { Validators } from '@angular/forms'");
		sbf.append("\nimport { ServiceAgent} from '").append(tsImportPrefix).append("serviceAgent';");
		/*
		 * import for child forms being referred
		 */
		if (this.linkedForms != null) {
			for (final LinkedForm child : this.linkedForms) {
				final String fn = child.getFormName();
				final String c = Util.toClassName(fn);
				sbf.append("\nimport { ").append(c).append("Form, ").append(c).append("Vo } from './").append(fn)
						.append("Form';");
			}
		}

		final String cls = Util.toClassName(this.name) + "Form";
		sbf.append("\n\nexport class ").append(cls).append(" extends Form {");
		sbf.append("\n\tprivate static _instance = new ").append(cls).append("();");

		/*
		 * form may not have all fields as control.As far as the client is
		 * concerned, only controls are assumed to be fields. so we ignore li
		 */

		/*
		 * controls/fields as members. We also accumulate code for controls
		 */
		final StringBuilder sbfCon = new StringBuilder();
		final StringBuilder listsSbf = new StringBuilder();
		if (this.controls != null) {
			for (final Control control : this.controls) {
				control.emitTs(sbf, sbfCon, this.fields, dataTypes, lists, keyedLists);
				/*
				 * populate the other two sbfs if required
				 */
				final String nam = control.name;
				final Field f = this.fields.get(nam);
				if (f != null) {
					final String s = Util.escapeTs(nam);

					if (f.listName != null) {
						if (listsSbf.length() > 0) {
							listsSbf.append(',');
						}
						listsSbf.append(s);
					}
				}
			}
		}
		/*
		 * child forms as members
		 */
		if (this.linkedForms != null && this.linkedForms.length != 0) {
			sbf.append("\n");
			for (final LinkedForm child : this.linkedForms) {
				child.emitTs(sbf);
			}
		}

		/*
		 * getInstance method
		 */
		sbf.append("\n\n\tpublic static getInstance(): ").append(cls).append(" {");
		sbf.append("\n\t\treturn ").append(cls).append("._instance;\n\t}");

		/*
		 * constructor
		 */
		sbf.append("\n\n\tconstructor() {");
		sbf.append("\n\t\tsuper();");

		/*
		 * put fields into a map.
		 */
		sbf.append("\n\t\tthis.fields = new Map();");

		sbf.append("\n\t\tthis.controls = new Map();").append(sbfCon.toString());

		/*
		 * put child forms into an array
		 */
		if (this.linkedForms != null && this.linkedForms.length != 0) {
			sbf.append("\n\n\t\tthis.childForms = new Map();");
			for (final LinkedForm child : this.linkedForms) {
				sbf.append("\n\t\tthis.childForms.set('").append(child.name).append("', this.").append(child.name)
						.append(");");
			}
		}

		/*
		 * auto-service operations?
		 */
		if (this.operations != null && this.operations.length > 0) {
			sbf.append("\n\t\tthis.opsAllowed = {");
			boolean first = true;
			for (String op : this.operations) {
				op = op.trim().toLowerCase();
				final Integer obj = OP_INDEXES.get(op);
				if (obj == null) {
					logger.error("{} is not a valid dbOperation. directive in allowDbOperations ignored", op);
				} else {
					if (first) {
						first = false;
					} else {
						sbf.append(C);
					}
					sbf.append(op).append(": true");
				}
			}
			sbf.append("};");
		}

		this.record.emitTs(sbf);
		/*
		 * fields with drop-downs
		 */
		if (listsSbf.length() > 0) {
			sbf.append("\n\t\tthis.listFields = [").append(listsSbf).append("];");
		}
		/*
		 * key fields
		 */
		if (this.keyFieldNames.size() > 0) {
			sbf.append("\n\t\tthis.keyFields = [");
			boolean firstOne = true;
			for (final String key : this.keyFieldNames) {
				if (firstOne) {
					firstOne = false;
				} else {
					sbf.append(C);
				}
				sbf.append('"').append(key).append('"');
			}
			sbf.append("];");
		}
		/*
		 * end of constructor
		 */
		sbf.append("\n\t}");

		sbf.append("\n\n\tpublic getName(): string {");
		sbf.append("\n\t\t return '").append(this.name).append("';");
		sbf.append("\n\t}");

		sbf.append("\n}\n");

		this.emitTsFormFd(sbf);
		this.emitTsFormVo(sbf, dataTypes);
	}

	/**
	 * create an interface for the data model of this form
	 */
	private void emitTsFormVo(final StringBuilder sbf, final Map<String, DataType> dataTypes) {
		sbf.append("\n\nexport interface ").append(Util.toClassName(this.name)).append("Vo extends Vo {");
		boolean isFirst = true;
		for (final Field field : this.fields.values()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			final DataType dt = dataTypes.get(field.dataType);
			sbf.append("\n\t").append(field.name).append("?: ").append(getTsValueType(dt));
		}
		if (this.linkedForms != null) {
			for (final LinkedForm lf : this.linkedForms) {
				if (isFirst) {
					isFirst = false;
				} else {
					sbf.append(C);
				}
				final String c = Util.toClassName(lf.getFormName());
				sbf.append("\n\t").append(lf.name).append("?: ").append(c).append("Vo");
			}
		}
		sbf.append("\n}\n");

	}

	/**
	 * extend form data to restrict name field to valid names of this form.
	 * Note that these operate only on form controls, and not on other fields.
	 * Non-control fields are manipulated through the Vo interface, with no
	 * support for two-way binding with the form. They would be one-way through
	 * vo.??
	 */
	private void emitTsFormFd(final StringBuilder sbf) {
		/*
		 * formFd extends FormData() to extend setFieldValue() and
		 * getFieldValue()
		 */
		final String cls = Util.toClassName(this.name);
		sbf.append("\n\nexport class ").append(cls).append("Fd extends FormData {");
		sbf.append("\n\tconstructor(form: ").append(cls).append("Form, sa: ServiceAgent) {");
		sbf.append("\n\t\tsuper(form, sa);\n\t}");

		if (this.controls == null || this.controls.length == 0) {
			sbf.append("\n\t/**  this form has no editable fields. data nust be accessed as Vo and not through fd **/");
			sbf.append("\n}\n");
			return;
		}

		/*
		 * names is like 'f1' | 'f2' | 'f3'
		 */
		final StringBuilder names = new StringBuilder();
		final char col = '\'';
		boolean isFirst = true;
		for (final Control control : this.controls) {
			if (isFirst) {
				isFirst = false;
			} else {
				names.append(" | ");
			}
			names.append(col).append(control.name).append(col);
		}

		final String types = "string | number | boolean | null";
		sbf.append("\n\n\tsetFieldValue(name: ").append(names).append(", value: ").append(types).append(" ): void {");
		sbf.append("\n\t\tsuper.setFieldValue(name, value);\n\t}");

		sbf.append("\n\n\tgetFieldValue(name: ").append(names).append(" ): ").append(types).append(" {");
		sbf.append("\n\t\treturn super.getFieldValue(name);\n\t}");
		sbf.append("\n}\n");
	}

	private static String getTsValueType(final DataType dt) {
		if (dt == null) {
			return "string";
		}
		switch (dt.valueType) {
		case Text:
		case Date:
		case Timestamp:
			return "string";

		case Integer:
		case Decimal:
			return "number";

		case Boolean:
			return "boolean";

		default:
			return "string";
		}
	}
}
