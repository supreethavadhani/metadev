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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class Form {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);

	private static final String C = ", ";

	String name;
	String schemaName;
	String[] dbOperations;
	Field[] localFields;
	Control[] controls;
	LinkedForm[] linkedForms;
	// Section[] sections;

	/*
	 * derived attributes
	 */
	Map<String, Field> fields;
	Schema schema;

	final Set<String> keyFieldNames = new HashSet<>();

	Field[] keyFields;

	void initialize(final Schema sch) {
		this.fields = new HashMap<>();

		if (sch != null) {
			this.schema = sch;
			for (final DbField f : sch.fieldMap.values()) {
				final ColumnType ct = f.getColumnType();
				if (ct == ColumnType.PrimaryKey || ct == ColumnType.GeneratedPrimaryKey) {
					this.keyFieldNames.add(f.name);
				}
			}

			this.fields.putAll(sch.fieldMap);
		}
		if (this.localFields != null) {
			int idx = 0;
			for (final Field f : this.localFields) {
				if (this.fields.containsKey(f.name)) {
					logger.error(
							"Schema has a field named {} but this is also being defined as a temp field. temp feld ignored",
							f.name);
					continue;
				}
				f.index = idx;
				idx++;
				this.fields.put(f.name, f);
			}
		}

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
		Util.emitImport(sbf, JsonObject.class);
		Util.emitImport(sbf, JsonArray.class);
		Util.emitImport(sbf, IServiceContext.class);
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

		final String cls = Util.toClassName(this.name);
		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form ").append(this.name).append("  */\npublic class ").append(cls)
				.append("Form extends Form {");

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
		getOps(this.dbOperations, sbf);

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
				this.linkedForms[i].emitJavaCode(sbf, this.fields);
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
		sbf.append("\n/** constructor */\npublic ").append(cls).append("Form() {");
		sbf.append(p).append("name = NAME;");
		if (schClass != null) {
			sbf.append(p).append("schema = ComponentProvider.getProvider().getSchema(SCHEMA);");
		}
		sbf.append(p).append("operations = OPS;");
		if (this.localFields != null) {
			sbf.append(p).append("localFields = FIELDS;");
		}

		if (this.linkedForms != null) {
			sbf.append(p).append("linkedForms = LINKED_FORMS;");
			sbf.append(p).append("initialize();");
		}

		sbf.append("\n\t}");

		/*
		 * methods to be implemented. Schema is cast to specific class if it is
		 * defined, else null is used
		 */
		p = "\n\n\t@Override\n\tpublic " + cls;

		/*
		 * newormData();
		 */
		sbf.append(p).append("Fd newFormData() {");
		sbf.append("\n\t\treturn new ").append(cls).append("Fd(this, null, null, null);\n\t}");

		/*
		 *
		 * public FormData parse(...)
		 */
		sbf.append(p).append("Fd  parse(final JsonObject json, final boolean forInsert, final IServiceContext ctx) {");
		sbf.append("\n\t\treturn (").append(cls).append("Fd)super.parse(json, forInsert, ctx);\n\t}");

		/*
		 *
		 * public FormData parse(...)
		 */
		sbf.append(p).append("Fd  parseKeys(final JsonObject json, final IServiceContext ctx) {");
		sbf.append("\n\t\treturn (").append(cls).append("Fd)super.parseKeys(json, ctx);\n\t}");

		/*
		 * public formDataTable parseTable(...)
		 */
		sbf.append(p).append(
				"Fdt  parseTable(final JsonArray arr, final boolean forInsert, final IServiceContext ctx, final String tableName) {");
		sbf.append("\n\t\treturn (").append(cls).append("Fdt)super.parseTable(arr, forInsert, ctx, tableName);\n\t}");

		/*
		 * newFormDataTable();
		 */
		sbf.append(p).append("Fdt newFormDataTable() {");
		sbf.append("\n\t\treturn new ").append(cls).append("Fdt(this, null, null, null);\n\t}");

		/*
		 * newFormData(schemaData, values, data)
		 */
		sbf.append(p).append(
				"Fd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {");
		sbf.append("\n\t\treturn new ").append(cls).append("Fd(this, ");
		if (schClass != null) {
			sbf.append("(").append(schClass).append("Data) schemaData");
		} else {
			sbf.append("null");
		}
		sbf.append(", values, data);\n\t}");

		/*
		 * newFormDataTable(table, values);
		 */
		sbf.append(p).append(
				"Fdt newFormDataTable(final SchemaDataTable table, final Object[][] values, FormDataTable[][] linkedData) {");
		sbf.append("\n\t\treturn new ").append(cls).append("Fdt(this, ");
		if (schClass != null) {
			sbf.append("(").append(schClass).append("DataTable) table");
		} else {
			sbf.append("null");
		}
		sbf.append(", values, linkedData);\n\t}");

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
		if (this.schemaName == null) {
			Util.emitImport(sbf, SchemaData.class);
		} else {
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
		sbf.append("\n/** class for form data ").append(this.name).append("  */");
		sbf.append("\npublic class ").append(cls).append("Fd extends FormData {");

		/*
		 * constructor
		 */
		sbf.append("\n\tpublic ").append(cls).append("Fd(final ").append(cls).append("Form form, final ");
		if (schClass == null) {
			sbf.append("SchemaData");
		} else {
			sbf.append(schClass).append("Data");
		}
		sbf.append(" dataObject, final Object[] values, final FormDataTable[] data) {");
		sbf.append("\n\t\tsuper(form, dataObject, values, data);");
		sbf.append("\n\t}");

		/*
		 * override getSchemaData() to return concrete class
		 */
		if (schClass != null) {
			sbf.append("\n\n\t@Override\n\tpublic ").append(schClass).append("Data getSchemaData() {");
			sbf.append("\n\t\treturn (").append(schClass).append("Data) this.dataObject;\n\t}");
		}
		/*
		 * setters and getters in case we have local fields
		 */
		if (this.localFields != null) {
			Generator.emitJavaGettersAndSetters(this.localFields, sbf, dataTypes);
		}

		/*
		 * getters for linked table if required
		 */
		if (this.linkedForms != null) {
			for (final LinkedForm lf : this.linkedForms) {
				lf.emitJavaGetter(sbf);
			}
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
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, FormDataTable.class);
		String schClass = null;
		if (this.schemaName == null) {
			Util.emitImport(sbf, SchemaDataTable.class);
		} else {
			schClass = Util.toClassName(this.schemaName);
			sbf.append("\nimport ").append(packageName).append(".schema.").append(schClass).append("DataTable;");
		}

		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form data table ").append(this.name).append("  */");
		sbf.append("\npublic class ").append(cls).append("Fdt extends FormDataTable {");

		/*
		 * constructor
		 */
		sbf.append("\n\tpublic ").append(cls).append("Fdt(final ").append(cls).append("Form form, final ");
		if (schClass == null) {
			sbf.append("Schema");
		} else {
			sbf.append(schClass);
		}
		sbf.append("DataTable dataTable, final Object[][] values, FormDataTable[][] linkedData) {");
		sbf.append("\n\t\tsuper(form, dataTable, values, linkedData);");
		sbf.append("\n\t}");

		/*
		 * getSchemaDataTable()
		 */
		if (schClass != null) {
			sbf.append("\n\n\t@Override\n\tpublic ").append(schClass).append("DataTable getDataTable() {");
			sbf.append("\n\t\t return (").append(schClass).append("DataTable) this.dataTable;\n\t}");
		}

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
				sbf.append("\nimport { ").append(Util.toClassName(fn)).append("Form } from './").append(fn)
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
		 * fields as members. We also accumulate code for controls
		 */
		final StringBuilder sbfCon = new StringBuilder();
		final StringBuilder keysSbf = new StringBuilder();
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

					if (this.keyFieldNames.contains(nam)) {
						if (keysSbf.length() > 0) {
							keysSbf.append(',');
						}
						keysSbf.append(s);
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
		if (this.dbOperations != null && this.dbOperations.length > 0) {
			sbf.append("\n\t\tthis.opsAllowed = {");
			boolean first = true;
			for (String op : this.dbOperations) {
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
		if (this.schema != null) {
			this.schema.emitTs(sbf);
		}
		/*
		 * fields with drop-downs
		 */
		if (listsSbf.length() > 0) {
			sbf.append("\n\t\tthis.listFields = [").append(listsSbf).append("];");
		}
		/*
		 * key fields
		 */
		if (keysSbf.length() > 0) {
			sbf.append("\n\t\tthis.keyFields = [").append(keysSbf).append("];");
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
		sbf.append("\n}\n");

	}

	/**
	 * extend form data to restrict name field to valid names of this form
	 */
	private void emitTsFormFd(final StringBuilder sbf) {
		final char col = '\'';
		/*
		 * names is like 'f1' | 'f2' | 'f3'
		 */
		final StringBuilder names = new StringBuilder();
		boolean isFirst = true;
		for (final Field field : this.fields.values()) {
			if (isFirst) {
				isFirst = false;
			} else {
				names.append(" | ");
			}
			names.append(col).append(field.name).append(col);
		}
		/*
		 * formFd extends FormData() to extend setFieldValue() and
		 * getFieldValue()
		 */
		final String cls = Util.toClassName(this.name);
		sbf.append("\n\nexport class ").append(cls).append("Fd extends FormData {");
		sbf.append("\n\tconstructor(form: ").append(cls).append("Form, sa: ServiceAgent) {");
		sbf.append("\n\t\tsuper(form, sa);\n\t}");

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
