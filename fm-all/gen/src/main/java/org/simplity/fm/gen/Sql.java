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

import java.util.Map;

import org.simplity.fm.core.data.ValueObject;
import org.simplity.fm.core.rdb.FilterSql;
import org.simplity.fm.core.rdb.ReadSql;
import org.simplity.fm.core.rdb.WriteSql;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Sql {
	private static final Logger logger = LoggerFactory.getLogger(Sql.class);
	private static final String P = "\n\tprivate static final ";
	private static final String SQL_TYPE_FILTER = "filter";
	private static final String SQL_TYPE_READ = "read";
	private static final String SQL_TYPE_WRITE = "write";

	String sqlType;
	String sql;
	Field[] sqlParams;
	Field[] outputFields;
	String schemaName;

	void emitJava(final StringBuilder sbf, final String packageName, final String className, final String dataTypesName,
			final Map<String, DataType> dataTypes) {
		sbf.append("package ").append(packageName).append(".sql;\n");

		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, ValueObject.class);
		sbf.append("\nimport ").append(packageName).append('.').append(dataTypesName).append(';');

		String baseCls = null;
		boolean hasOutput = true;
		boolean ok = true;
		String sqlPrefix = "";
		boolean isFilter = false;
		if (this.sqlType.equals(SQL_TYPE_FILTER)) {
			if (this.schemaName == null || this.schemaName.isEmpty()) {
				logger.error("schemaName is required for filter sql");
				ok = false;
			} else {
				final String txt = this.sql.trim().toLowerCase();
				if (txt.startsWith("where")) {
					logger.error(
							"Sql for filter should not start with WHERE. It should assume that a where is already present in the sqland provide the rest of the sql.");
					ok = false;
				} else {
					hasOutput = false;
					isFilter = true;
					sqlPrefix = " WHERE ";
					final String schemaCls = Util.toClassName(this.schemaName);
					baseCls = "FilterSql<" + schemaCls + "Data" + ", " + schemaCls + "DataTable>";
					sbf.append("\nimport ").append(packageName).append(".schema.").append(schemaCls).append("Schema;");
					sbf.append("\nimport ").append(packageName).append(".schema.").append(schemaCls).append("Data;");
					sbf.append("\nimport ").append(packageName).append(".schema.").append(schemaCls)
							.append("DataTable;");
					Util.emitImport(sbf, FilterSql.class);
				}
			}
		} else if (this.schemaName != null && this.schemaName.isEmpty() == false) {
			logger.error("Schema name should not be specified for a non-filter sql.");
			ok = false;
		} else if (this.sqlType.equals(SQL_TYPE_WRITE)) {
			baseCls = "WriteSql";
			Util.emitImport(sbf, WriteSql.class);
			hasOutput = false;

		} else if (this.sqlType.equals(SQL_TYPE_READ)) {
			baseCls = "ReadSql<" + className + ".OutputVo>";
			Util.emitImport(sbf, ReadSql.class);
		} else {
			logger.error("{} is not a valid sqlType", this.sqlType);
			ok = false;
		}

		if (!ok) {
			sbf.append("Class not generated becaue sqlType is set to '").append(this.sqlType);
			sbf.append("while we expect one of read,write,filter ");
			return;
		}
		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(className).append(" */");
		sbf.append("\npublic class ").append(className).append(" extends ").append(baseCls).append(" {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ").append(Util.escape(sqlPrefix + this.sql)).append(';');
		if (this.sqlParams != null) {
			sbf.append(P).append("Field[] IN = ");
			emitFields(sbf, this.sqlParams, dataTypesName);
			sbf.append(';');
		}

		if (hasOutput) {
			if (this.outputFields == null || this.outputFields.length == 0) {
				logger.error("read sqls must define output fields");
				sbf.append("ERROR: no output fields defined!!!");
				return;
			}
			sbf.append(P).append("Field[] OUT = ");
			emitFields(sbf, this.outputFields, dataTypesName);
			sbf.append(';');
		}

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ").append(className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		if (this.sqlParams != null) {
			sbf.append("\n\t\tthis.inputData = new ValueObject(IN, null);");
			if (isFilter) {
				sbf.append("\n\t\tthis.schema = new ").append(Util.toClassName(this.schemaName)).append("Schema();");
			}
		}
		sbf.append("\n\t}");

		if (this.sqlParams != null) {
			this.emitSetters(sbf, dataTypes);
		}
		if (hasOutput) {
			this.emitOutMethods(sbf, dataTypes);
		} else if (!isFilter) {
			emitWriteMethods(sbf);
		}
		sbf.append("\n}\n");
	}

	private static void emitWriteMethods(final StringBuilder sbf) {
		sbf.append("\n\n\t@Override\n\tprotected ValueObject newValueObject() {");
		sbf.append("\n\t\treturn new ValueObject(IN, null);");
		sbf.append("\n\t}");
	}

	private void emitOutMethods(final StringBuilder sbf, final Map<String, DataType> dataTypes) {
		/*
		 * abstract method to be implemented
		 */
		sbf.append("\n\n\t@Override\n\tprotected ");
		if (this.sqlType.equals(SQL_TYPE_FILTER)) {
			sbf.append("ValueTable<OutputVo> newValueTable() {");
			sbf.append("\n\t\treturn new ValueTable<>(new OutputVo(OUT));\n\t}");
		} else {
			sbf.append("OutputVo newOutputData() {");
			sbf.append("\n\t\treturn new OutputVo(OUT);\n\t}");
		}
		/*
		 * inner class for custom Vo
		 */

		sbf.append("\n\n\t/** VO with output fields from this Sql */");
		sbf.append("\n\tpublic static class OutputVo extends ValueObject {");

		sbf.append("\n\n\t\t/**\n\t\t * @param fields\n\t\t */");
		sbf.append("\n\t\tpublic OutputVo(final Field[] fields) {");
		sbf.append("\n\t\t\tsuper(fields, null);\n\t\t}");

		this.emitGetters(sbf, dataTypes);
		sbf.append("\n\t}");

	}

	private void emitSetters(final StringBuilder sbf, final Map<String, DataType> dataTypes) {
		for (final Field f : this.sqlParams) {
			final DataType dt = dataTypes.get(f.dataType);
			String typ = "unknownBecauseOfUnknownDataType";
			if (dt == null) {
				logger.error("Field {} has an invalid data type of {}", f.name, f.dataType);
			} else {
				typ = Util.JAVA_VALUE_TYPES[dt.valueType.ordinal()];
			}
			final String nam = f.name;
			final String cls = Util.toClassName(nam);

			sbf.append("\n\n\t/**\n\t * set value for ").append(nam);
			sbf.append("\n\t * @param value to be assigned to ").append(nam);
			sbf.append("\n\t */");
			sbf.append("\n\tpublic void set").append(cls).append('(').append(typ).append(" value){");
			sbf.append("\n\t\tthis.inputData.setValue(").append(f.index).append(", value);");
			sbf.append("\n\t}");
		}
	}

	/**
	 * @param constr
	 * @param dataTypes
	 */
	private void emitGetters(final StringBuilder sbf, final Map<String, DataType> dataTypes) {
		for (final Field f : this.outputFields) {
			final DataType dt = dataTypes.get(f.dataType);
			String typ = "unknownBecauseOfUnknownDataType";
			String get = typ;
			if (dt == null) {
				logger.error("Field {} has an invalid data type of {}", f.name, f.dataType);
			} else {
				typ = Util.JAVA_VALUE_TYPES[dt.valueType.ordinal()];
				get = Util.JAVA_GET_TYPES[dt.valueType.ordinal()];
			}
			final String nam = f.name;
			final String cls = Util.toClassName(nam);

			sbf.append("\n\n\t\t/**\n\t * @return value of ").append(nam).append("\n\t */");
			sbf.append("\n\t\tpublic ").append(typ).append(" get").append(cls).append("(){");
			sbf.append("\n\t\t\treturn super.get").append(get).append("Value(").append(f.index).append(");");
			sbf.append("\n\t\t}");
		}
	}

	private static void emitFields(final StringBuilder sbf, final Field[] fields, final String dtName) {
		sbf.append('{');
		int idx = -1;
		for (final Field field : fields) {
			idx++;
			if (idx > 0) {
				sbf.append(',');
			}
			field.index = idx;
			field.emitJavaCode(sbf, dtName);
		}
		sbf.append('}');
	}
}
