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

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
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
	String schemaName;
	String[] operations;
	Field[] localFields;
	Control[] controls;
	LinkedForm[] linkedForms;
	// Section[] sections;

	/*
	 * derived attributes
	 */
	Map<String, Field> fields;
	Schema schema;

	Field[] fieldsWithList;

	Field[] keyFields;

	void initialize(final Schema sch) {
		this.schema = sch;
		this.fields = new HashMap<>();
		final List<Field> listFields = new ArrayList<>();
		final List<Field> keyedFields = new ArrayList<>();
		for (final DbField f : sch.fieldMap.values()) {
			if (f.listName != null) {
				listFields.add(f);
			}
			final ColumnType ct = f.getColumnType();
			if (ct == ColumnType.PrimaryKey || ct == ColumnType.GeneratedPrimaryKey) {
				keyedFields.add(f);
			}
		}
		this.fields.putAll(sch.fieldMap);
		if (this.localFields != null) {
			for (final Field f : this.localFields) {
				if (this.fields.containsKey(f.name)) {
					logger.error(
							"Schema has a field named {} but this is also being defined as a temp field. temp feld ignored",
							f.name);
					continue;
				}

				this.fields.put(f.name, f);
				if (f.listName != null) {
					listFields.add(f);
				}
			}
		}

		if (listFields.size() > 0) {
			this.fieldsWithList = listFields.toArray(new Field[0]);
		}

		if (keyedFields.size() > 0) {
			this.keyFields = keyedFields.toArray(new Field[0]);
		}
	}

	void emitJavaForm(final StringBuilder sbf, final String packageName) {
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");
		Util.emitImport(sbf, ComponentProvider.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.LinkedForm.class);
		Util.emitImport(sbf, FormDataTable.class);
		Util.emitImport(sbf, FormData.class);
		Util.emitImport(sbf, SchemaData.class);
		Util.emitImport(sbf, SchemaDataTable.class);
		/*
		 * data types are directly referred to the static declarations
		 */
		sbf.append("\nimport ").append(packageName).append('.').append(Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME)
				.append(';');

		String schClass = null;
		if (this.schema != null) {
			schClass = Util.toClassName(this.schemaName);
			final String imp = "\nimport " + packageName + ".schema.";
			sbf.append(imp).append(schClass).append("Data;");
			sbf.append(imp).append(schClass).append("DataTable;");
		}

		/*
		 * class declaration
		 */
		final String cls = Util.toClassName(this.name);
		sbf.append("\n/** class for form ").append(this.name).append("  */\npublic class ").append(cls)
				.append(" extends Form {");

		String p = "\n\tprotected static final ";

		/*
		 * protected static final Field[] FIELDS = {.....};
		 */
		sbf.append(p).append("String NAME = ").append(Util.escape(this.name)).append(';');
		/*
		 * protected static final String SCHEMA = "....";
		 */
		if (schClass != null) {
			sbf.append(p).append("String SCHEMA = ").append(Util.escape(this.schemaName)).append(';');
		}

		/*
		 * protected static final Field[] FIELDS = {.....};
		 */
		if (this.localFields != null) {
			sbf.append(p).append(" Field[] FIELDS = {");
			for (final Field field : this.localFields) {
				field.emitJavaCode(sbf, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);
				sbf.append(C);
			}
			sbf.setLength(sbf.length() - C.length());

			sbf.append("};");
		}

		/*
		 * protected static final boolean[] OPS = {true, false,..};
		 */
		sbf.append(p);
		getOps(this.operations, sbf);

		/*
		 * protected static final LinkedForm[] LINKED_FORMS = {......};
		 */
		if (this.linkedForms != null) {
			sbf.append(p).append("LinkedForm[] LINKED_FORMS = {");
			for (int i = 0; i < this.linkedForms.length; i++) {
				if (i != 0) {
					sbf.append(',');
				}
				sbf.append("\n\t\t\t");
				this.linkedForms[i].emitJavaCode(sbf);
			}
			sbf.append("};");
		}
		/*
		 * public ClassName(){
		 * this.name = NAME;
		 * this.schema = ComponentProvider.getProvider().getSchema(schemaName);
		 * this.fields = FIELDS;
		 * this.operations = OPS;
		 * this.linkedForms = LINKED_FORMS;
		 * initialize();
		 * }
		 */
		p = "\n\t\tthis.";
		sbf.append("\n/** constructor */\npublic ").append(cls).append("() {");
		sbf.append(p).append("name = NAME;");
		if (schClass != null) {
			sbf.append(p).append("schema = ComponentProvider.getProvider().getSchema(SCHEMA);");
		}
		sbf.append(p).append("operations = OPS;");
		if (this.linkedForms != null) {
			sbf.append(p).append("linkedForms = LINKED_FORMS;");
			sbf.append(p).append("initialize();");
		}

		sbf.append("\n\t}");

		/*
		 * methods to be implemented. Schema is cast to specific class if it is
		 * defined, else null is used
		 */
		p = "\n\n\t@Override\n\tprotected " + cls;

		sbf.append(p).append(
				"Data newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {");
		sbf.append("\n\t\treturn new ").append(cls).append("Data(this, ");
		if (schClass != null) {
			sbf.append("(").append(schClass).append("Data) schemaData");
		} else {
			sbf.append("null");
		}
		sbf.append(", values, data);\n\t}");

		sbf.append(p).append("DataTable newFormDataTable(final SchemaDataTable table, final Object[][] values) {");
		sbf.append("\n\t\treturn new ").append(cls).append("DataTable(this, ");
		if (schClass != null) {
			sbf.append("(").append(schClass).append("DataTable) table");
		} else {
			sbf.append("null");
		}
		sbf.append(", values);\n\t}");

		sbf.append("\n}\n");
	}

	void emitJavaFormData(final StringBuilder sbf, final String packageName, final Map<String, DataType> dataTypes) {
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}

		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports.
		 */
		Util.emitImport(sbf, FormData.class);
		Util.emitImport(sbf, FormDataTable.class);
		String schClass = null;
		if (this.schemaName != null) {
			schClass = Util.toClassName(this.schemaName);
			sbf.append("\nimport ").append(packageName).append(".schema.").append(schClass).append("Data;");
		}

		if (this.localFields != null) {
			Util.emitImport(sbf, LocalDate.class);
			Util.emitImport(sbf, Instant.class);
		}

		/*
		 * class declaration
		 */
		final String cls = Util.toClassName(this.name);
		final String d = "Data";
		sbf.append("\n/** class for form data ").append(this.name).append("  */");
		sbf.append("\npublic class ").append(cls).append(d).append(" extends FormData {");

		/*
		 * constructor
		 */
		sbf.append("\n\tprotected ").append(cls).append(d).append("(final ").append(cls).append(" form, final ");
		if (schClass == null) {
			sbf.append("SchemaData");
		} else {
			sbf.append(schClass).append(d);
		}
		sbf.append(" dataObject, final Object[] values, final FormDataTable[] data) {");
		sbf.append("\n\t\tsuper(form, dataObject, values, data);");
		sbf.append("\n\t}");

		/*
		 * setters and getters in case we have local fields
		 */
		if (this.localFields != null) {
			Generator.emitJavaGettersAndSetters(this.localFields, sbf, dataTypes);
		}

		sbf.append("\n}\n");
	}

	void emitJavaFormDataTable(final StringBuilder sbf, final String packageName) {
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}

		final String cls = Util.toClassName(this.name);
		final String dt = "DataTable";
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, FormDataTable.class);
		String schClass = null;
		if (this.schemaName != null) {
			schClass = Util.toClassName(this.schemaName);
			sbf.append("\nimport ").append(packageName).append(".schema.").append(schClass).append(dt).append(";");
		}

		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form data table ").append(this.name).append("  */");
		sbf.append("\npublic class ").append(cls).append(dt).append(" extends FormDataTable {");

		/*
		 * constructor
		 */
		sbf.append("\n\tprotected ").append(cls).append(dt).append("(final ").append(cls).append(" form, final ");
		if (schClass == null) {
			sbf.append("Schema");
		} else {
			sbf.append(schClass);
		}
		sbf.append(dt).append(" dataTable, final Object[][] values) {");
		sbf.append("\n\t\tsuper(form, dataTable, values);");
		sbf.append("\n\t}");

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
		sbf.append("\nimport { SelectOption, Vo } from '").append(tsImportPrefix).append("types';");
		sbf.append("\nimport { Validators } from '@angular/forms'");
		/*
		 * import for child forms being referred
		 */
		if (this.linkedForms != null) {
			for (final LinkedForm child : this.linkedForms) {
				final String fn = child.getFormName();
				sbf.append("\nimport { ").append(Util.toClassName(fn)).append(" } from './").append(fn).append("';");
			}
		}

		final String cls = Util.toClassName(this.name);
		sbf.append("\n\nexport class ").append(cls).append(" extends Form {");
		sbf.append("\n\tprivate static _instance = new ").append(cls).append("();");

		/*
		 * fields as members. We also accumulate code for controls
		 */
		final StringBuilder sbfCon = new StringBuilder();
		if (this.controls != null) {
			for (final Control control : this.controls) {
				control.emitTs(sbf, sbfCon, this.fields, dataTypes, lists, keyedLists);
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

		if (this.localFields != null) {
			for (final Field field : this.localFields) {
				sbf.append("\n\t\tthis.fields.set('").append(field.name).append("', this.");
				sbf.append(field.name).append(");");
			}
		}
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

		/*
		 * inter field validations
		 */
		this.schema.emitTs(sbf);
		/*
		 * fields with drop-downs
		 */
		if (this.fieldsWithList != null) {
			sbf.append("\n\t\tthis.listFields = [");
			for (final Field f : this.fieldsWithList) {
				sbf.append(Util.escapeTs(f.name));
				sbf.append(C);
			}
			sbf.setLength(sbf.length() - C.length());
			sbf.append("];");
		}
		/*
		 * are there key fields?
		 */
		if (this.keyFields != null) {
			sbf.append("\n\t\tthis.keyFields = [");
			for (final Field f : this.keyFields) {
				sbf.append(Util.escapeTs(f.name));
				sbf.append(C);
			}
			sbf.setLength(sbf.length() - C.length());
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

		this.emitTsFormData(sbf, dataTypes);

	}

	/**
	 * create an interface for the data model of this form
	 */
	private void emitTsFormData(final StringBuilder sbf, final Map<String, DataType> dataTypes) {
		sbf.append("\n\nexport interface ").append(Util.toClassName(this.name)).append("Data extends Vo {");
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
