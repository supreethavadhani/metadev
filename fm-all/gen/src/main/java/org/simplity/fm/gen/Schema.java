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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.core.validn.ExclusiveValidation;
import org.simplity.fm.core.validn.FromToValidation;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.validn.InclusiveValidation;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * represents the contents of a spread sheet for a form
 *
 * @author simplity.org
 *
 */
class Schema {
	/*
	 * this logger is used by all related classes of form to give the programmer
	 * the right stream of logs to look for any issue in the workbook
	 */
	static final Logger logger = LoggerFactory.getLogger(Schema.class);

	private static final String C = ", ";
	private static final String P = "\n\tprivate static final ";

	/*
	 * fields that are read directly from json
	 */
	String name;
	String nameInDb;
	boolean useTimestampCheck;
	String customValidation;
	String[] dbOperations;
	/*
	 * reason we have it as an array rather than a MAP is that the sequence,
	 * though not recommended, could be hard-coded by some coders
	 */
	DbField[] fields;
	FromToPair[] fromToPairs;
	ExclusivePair[] exclusivePairs;
	InclusivePair[] inclusivePairs;

	/*
	 * derived fields required for generating java/ts
	 */
	Map<String, DbField> fieldMap;
	DbField[] fieldsWithList;
	DbField[] keyFields;

	DbField tenantField;
	DbField timestampField;
	DbField generatedKeyField;

	/*
	 * some tables may have primary key, but not have anything to update
	 */
	transient boolean isUpdatable;

	void init() {
		/*
		 * we want to check for duplicate definition of standard fields
		 */
		DbField modifiedAt = null;
		DbField modifiedBy = null;
		DbField createdBy = null;
		DbField createdAt = null;

		this.fieldMap = new HashMap<>();
		final List<DbField> list = new ArrayList<>();
		final List<DbField> keyList = new ArrayList<>();

		int idx = -1;
		for (final DbField field : this.fields) {
			idx++;
			field.index = idx;
			final String fieldName = field.name;

			if (this.fieldMap.containsKey(fieldName)) {
				logger.error("{} is a duplicate field ", fieldName);
			} else {
				this.fieldMap.put(fieldName, field);
			}

			if (field.listName != null) {
				list.add(field);
			}

			ColumnType ct = field.getColumnType();
			if (ct == null) {
				if (field.dbColumnName == null) {
					logger.warn("{} is not linked to a db-column. No I/O happens on this field.", fieldName);
					continue;
				}
				logger.error(
						"{} is linked to a db-column {} but does not specify a db-column-type. it is treated as an optionl field.",
						fieldName, field.dbColumnName);
				ct = ColumnType.OptionalData;
			}

			field.isRequired = ct.isRequired();

			switch (ct) {
			case PrimaryKey:
				if (this.generatedKeyField != null) {
					logger.error("{} is defined as a generated primary key, but {} is also defined as a primary key.",
							keyList.get(0).name, field.name);
				} else {
					keyList.add(field);
				}
				continue;

			case GeneratedPrimaryKey:
				if (this.generatedKeyField != null) {
					logger.error("Only one generated key please. Found {} as well as {} as generated primary keys.",
							field.name, keyList.get(0).name);
				} else {
					if (keyList.size() > 0) {
						logger.error(
								"Field {} is marked as a generated primary key. But {} is also marked as a primary key field.",
								field.name, keyList.get(0).name);
						keyList.clear();
					}
					keyList.add(field);
					this.generatedKeyField = field;
				}
				continue;

			case TenantKey:
				if (field.dataType.equals("tenantKey") == false) {
					logger.error(
							"Tenant key field MUST use dataType of tenantKey. Field {} which is marked as tenant key is of data type {}",
							field.name, field.dataType);
				}
				if (this.tenantField == null) {
					this.tenantField = field;
				} else {
					logger.error("Both {} and {} are marked as tenantKey. Tenant key has to be unique.", field.name,
							this.tenantField.name);
				}
				continue;

			case CreatedAt:
				if (createdAt == null) {
					createdAt = field;
				} else {
					logger.error("Only one field to be used as createdAt but {} and {} are marked", field.name,
							createdAt.name);
				}
				continue;

			case CreatedBy:
				if (createdBy == null) {
					createdBy = field;
				} else {
					logger.error("Only one field to be used as createdBy but {} and {} are marked", field.name,
							createdBy.name);
				}
				continue;

			case ModifiedAt:
				if (modifiedAt == null) {
					modifiedAt = field;
					if (this.useTimestampCheck) {
						this.timestampField = field;
					}
				} else {
					logger.error("{} and {} are both defined as lastModifiedAt!!", field.name,
							this.timestampField.name);
				}
				continue;

			case ModifiedBy:
				if (modifiedBy == null) {
					modifiedBy = field;
				} else {
					logger.error("Only one field to be used as modifiedBy but {} and {} are marked", field.name,
							modifiedBy.name);
				}
				continue;

			default:
				continue;
			}
		}

		if (list.size() > 0) {
			this.fieldsWithList = list.toArray(new DbField[0]);
		}

		if (keyList.size() > 0) {
			this.keyFields = keyList.toArray(new DbField[0]);
		}

		if (this.useTimestampCheck && this.timestampField == null) {
			logger.error(
					"Table is designed to use time-stamp for concurrancy, but no field with columnType=modifiedAt");
			this.useTimestampCheck = false;
		}

	}

	Set<String> getNameSet() {
		if (this.fieldMap != null) {
			return this.fieldMap.keySet();
		}
		return new HashSet<>();
	}

	void emitJavaClass(final StringBuilder sbf, final String generatedPackage) {
		final String typesName = Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		String pck = generatedPackage + ".schema";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, org.simplity.fm.core.data.DbField.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Schema.class);
		Util.emitImport(sbf, IValidation.class);
		Util.emitImport(sbf, DbAssistant.class);
		Util.emitImport(sbf, IServiceContext.class);
		Util.emitImport(sbf, JsonObject.class);
		Util.emitImport(sbf, JsonArray.class);
		Util.emitImport(sbf, ColumnType.class);

		/*
		 * validation imports on need basis
		 */
		if (this.fromToPairs != null) {
			Util.emitImport(sbf, FromToValidation.class);
		}
		if (this.exclusivePairs != null) {
			Util.emitImport(sbf, ExclusiveValidation.class);
		}
		if (this.inclusivePairs != null) {
			Util.emitImport(sbf, InclusiveValidation.class);
		}
		Util.emitImport(sbf, DependentListValidation.class);
		/*
		 * data types are directly referred to the static declarations
		 */
		sbf.append("\nimport ").append(generatedPackage).append('.').append(typesName).append(';');
		/*
		 * class definition
		 */
		final String cls = Util.toClassName(this.name);

		sbf.append("\n\n/**\n * class that represents structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append("Schema extends Schema {");

		this.emitJavaFields(sbf, typesName);
		sbf.append("\n\tprivate static final ");
		Form.getOps(this.dbOperations, sbf);
		this.emitDbStuff(sbf);
		this.emitJavaValidations(sbf);

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**\n\t *\n\t */");
		sbf.append("\n\tpublic ").append(cls).append("Schema() {");
		sbf.append("\n\t\tthis.name = \"").append(this.name).append("\";");
		sbf.append("\n\t\tthis.fields = FIELDS;");
		sbf.append("\n\t\tthis.validations = VALIDS;");
		if (this.nameInDb != null) {
			sbf.append("\n\t\tthis.nameInDb = ").append(Util.escape(this.nameInDb)).append(";");
			sbf.append("\n\t\tthis.operations = OPS;");
			this.emitDbMeta(sbf);
		}
		sbf.append("\n\t\tthis.initialize();");
		sbf.append("\n\t}");

		/*
		 * concrete methods to create concrete data instances
		 */
		final String over = "\n\n\t@Override";

		sbf.append(over);

		String c = cls + "Data";
		sbf.append("\n\tpublic ").append(c).append(" newSchemaData() {");
		sbf.append("\n\t\treturn new ").append(c).append("(this, null);");
		sbf.append("\n\t}");

		sbf.append(over);
		sbf.append("\n\tprotected ").append(c).append(" newSchemaData(final Object[] data) {");
		sbf.append("\n\t\treturn new ").append(c).append("(this, data);");
		sbf.append("\n\t}");

		sbf.append(over);
		sbf.append("\n\tpublic ").append(c).append(
				" parseData(JsonObject json, boolean forInsert, IServiceContext ctx, String tableName, int row) {");
		sbf.append("\n\t\treturn (").append(c).append(") super.parseData(json, forInsert, ctx, tableName, row);");
		sbf.append("\n\t}");

		sbf.append(over);
		sbf.append("\n\tpublic ").append(c).append(" parseKeys(JsonObject json, IServiceContext ctx) {");
		sbf.append("\n\t\treturn (").append(c).append(") super.parseKeys(json, ctx);");
		sbf.append("\n\t}");

		c = cls + "DataTable";
		sbf.append(over);
		sbf.append("\n\tpublic ").append(c).append(" newSchemaDataTable() {");
		sbf.append("\n\t\treturn new ").append(c).append("(this, null);");
		sbf.append("\n\t}");

		sbf.append(over);
		sbf.append("\n\tprotected ").append(c).append(" newSchemaDataTable(final Object[][] data) {");
		sbf.append("\n\t\treturn new ").append(c).append("(this, data);");
		sbf.append("\n\t}");

		sbf.append(over);
		sbf.append("\n\tpublic ").append(c)
				.append(" parseTable(JsonArray arr, boolean forInsert, IServiceContext ctx, String tableName) {");
		sbf.append("\n\t\treturn (").append(c).append(") super.parseTable(arr, forInsert, ctx, tableName);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");
	}

	private void emitDbStuff(final StringBuilder sbf) {
		if (this.nameInDb == null || this.nameInDb.isEmpty()) {
			if (this.dbOperations != null) {
				final String msg = "nameInDb not specified, but dbOpertions specified. No db opertion is possible without a table/view name";
				logger.error(msg);
				sbf.append("\n//compiltion error created to highlight an error\nint a = \"").append(msg).append("\";");
			} else {
				logger.warn("dbName not set. no db related code generated for this form");
			}
			return;
		}

		this.emitSelect(sbf);
		if (this.keyFields == null) {
			logger.debug(
					"No keys defined for the db table. only filter operation is allowed. Other operations require primary key/s.");
		} else {
			this.emitInsert(sbf);

			/*
			 * indexes is to be built like "1,2,3,4"
			 */
			final StringBuilder indexes = new StringBuilder();
			/*
			 * clause is going to be like " WHERE k1=? AND k2=?...."
			 */
			final StringBuilder clause = new StringBuilder();
			this.makeWhere(clause, indexes, this.keyFields);

			sbf.append(P).append("String WHERE = \"").append(clause.toString()).append("\";");
			sbf.append(P).append("int[] WHERE_IDX = {").append(indexes.toString()).append("};");

			this.emitUpdate(sbf, clause.toString(), indexes.toString());
			sbf.append(P).append("String DELETE = \"DELETE FROM ").append(this.nameInDb).append("\";");
		}
	}

	private void emitJavaFields(final StringBuilder sbf, final String dataTypesName) {
		sbf.append("\tprivate static final DbField[] FIELDS = ");
		if (this.fields == null) {
			sbf.append("null;");
			return;
		}
		sbf.append("{");
		boolean isFirst = true;
		for (final DbField field : this.fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			field.emitJavaCode(sbf, dataTypesName);
		}
		sbf.append("\n\t};");
	}

	private void emitJavaValidations(final StringBuilder sbf) {
		sbf.append("\n\tprivate static final IValidation[] VALIDS = {");
		final int n = sbf.length();
		final String sufix = ",\n\t\t";
		if (this.fromToPairs != null) {
			for (final FromToPair pair : this.fromToPairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		if (this.exclusivePairs != null) {
			for (final ExclusivePair pair : this.exclusivePairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		if (this.inclusivePairs != null) {
			for (final InclusivePair pair : this.inclusivePairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		/*
		 * dependent lists
		 */
		if (this.fieldsWithList != null) {
			for (final DbField field : this.fieldsWithList) {
				if (field.listKey == null) {
					continue;
				}
				final DbField f = this.fieldMap.get(field.listKey);
				if (f == null) {
					logger.error("DbField {} specifies {} as listKey, but that field is not defined", field.name,
							field.listKey);
					continue;
				}

				sbf.append("new DependentListValidation(").append(field.index);
				sbf.append(C).append(f.index);
				sbf.append(C).append(Util.escape(field.listName));
				sbf.append(C).append(Util.escape(field.name));
				sbf.append(C).append(Util.escape(field.errorId));
				sbf.append(")");
				sbf.append(sufix);
			}
		}

		if (sbf.length() > n) {
			/*
			 * remove last sufix
			 */
			sbf.setLength(sbf.length() - sufix.length());
		}

		sbf.append("\n\t};");
	}

	private void emitDbMeta(final StringBuilder sbf) {

		sbf.append("\n\n\t\tthis.dbAssistant = new DbAssistant(").append(this.fields.length);
		if (this.tenantField == null) {
			sbf.append(", null");
		} else {
			sbf.append(", this.fields[").append(this.tenantField.index).append("]");
		}

		sbf.append(", SELECT, this.getParams(SELECT_IDX)");

		if (this.keyFields != null) {
			sbf.append(", WHERE, this.getParams(WHERE_IDX)");
			sbf.append(", INSERT, this.getParams(INSERT_IDX)");
			if (this.isUpdatable) {
				sbf.append(", UPDATE, this.getParams(UPDATE_IDX)");
			} else {
				sbf.append(", null, null");
			}
			sbf.append(", DELETE, ");
			if (this.generatedKeyField == null) {
				sbf.append("null, -1");
			} else {
				sbf.append(Util.escape(this.generatedKeyField.dbColumnName));
				sbf.append(", ").append(this.generatedKeyField.index);
			}
			if (this.timestampField == null) {
				sbf.append(", null");
			} else {
				sbf.append(", this.fields[").append(this.timestampField.index).append("]");
			}
		}
		sbf.append(");");

	}

	private void makeWhere(final StringBuilder clause, final StringBuilder indexes, final DbField[] keys) {
		clause.append(" WHERE ");
		boolean firstOne = true;
		for (final DbField field : keys) {
			if (firstOne) {
				firstOne = false;
			} else {
				clause.append(" AND ");
				indexes.append(C);
			}
			clause.append(field.dbColumnName).append("=?");
			indexes.append(field.index);
		}
		/*
		 * as a matter of safety, tenant key is always part of queries
		 */
		if (this.tenantField != null) {
			clause.append(" AND ").append(this.tenantField.dbColumnName).append("=?");
			indexes.append(C).append(this.tenantField.index);
		}
	}

	private void emitSelect(final StringBuilder sbf) {
		final StringBuilder idxSbf = new StringBuilder();
		sbf.append(P).append("String SELECT = \"SELECT ");

		boolean firstOne = true;
		for (final DbField field : this.fields) {
			final ColumnType ct = field.getColumnType();
			if (ct == null) {
				continue;
			}
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
				idxSbf.append(C);
			}
			sbf.append(field.dbColumnName);
			idxSbf.append(field.index);
		}

		sbf.append(" FROM ").append(this.nameInDb);
		sbf.append("\";");
		sbf.append(P).append("int[] SELECT_IDX = {").append(idxSbf).append("};");

	}

	private void emitInsert(final StringBuilder sbf) {
		sbf.append(P).append(" String INSERT = \"INSERT INTO ").append(this.nameInDb).append('(');
		final StringBuilder idxSbf = new StringBuilder();
		idxSbf.append(P).append("int[] INSERT_IDX = {");
		final StringBuilder vbf = new StringBuilder();
		boolean firstOne = true;
		boolean firstField = true;
		for (final DbField field : this.fields) {
			final ColumnType ct = field.getColumnType();
			if (ct == null || ct.isInserted() == false) {
				continue;
			}
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
				vbf.append(C);
			}
			sbf.append(field.dbColumnName);
			if (ct == ColumnType.ModifiedAt || ct == ColumnType.CreatedAt) {
				vbf.append(" CURRENT_TIMESTAMP ");
			} else {
				vbf.append('?');
				if (firstField) {
					firstField = false;
				} else {
					idxSbf.append(C);
				}
				idxSbf.append(field.index);
			}
		}

		sbf.append(") values (").append(vbf).append(")\";");
		sbf.append(idxSbf).append("};");
	}

	private void emitUpdate(final StringBuilder sbf, final String whereClause, final String whereIndexes) {
		final StringBuilder updateBuf = new StringBuilder();
		updateBuf.append(P).append(" String UPDATE = \"UPDATE ").append(this.nameInDb).append(" SET ");

		final StringBuilder idxBuf = new StringBuilder();
		idxBuf.append(P).append(" int[] UPDATE_IDX = {");

		boolean firstOne = true;
		boolean firstField = true;
		for (final DbField field : this.fields) {
			final ColumnType ct = field.getColumnType();
			if (ct == null || ct.isUpdated() == false) {
				continue;
			}

			if (firstOne) {
				firstOne = false;
			} else {
				updateBuf.append(C);
			}

			updateBuf.append(field.dbColumnName).append("=");
			if (ct == ColumnType.ModifiedAt) {
				updateBuf.append(" CURRENT_TIMESTAMP ");
			} else {
				updateBuf.append(" ? ");
				if (firstField) {
					firstField = false;
				} else {
					idxBuf.append(C);
				}
				idxBuf.append(field.index);
			}
		}
		if (firstOne) {
			/*
			 * nothing to update
			 */
			this.isUpdatable = false;
			return;
		}
		this.isUpdatable = true;
		// update sql will have the where indexes at the end
		if (!firstField) {
			idxBuf.append(C);
		}
		idxBuf.append(whereIndexes);
		updateBuf.append(whereClause);

		if (this.useTimestampCheck) {
			updateBuf.append(" AND ").append(this.timestampField.dbColumnName).append("=?");
			idxBuf.append(C).append(this.timestampField.index);
		}
		updateBuf.append("\";");
		sbf.append(updateBuf.toString()).append(idxBuf.toString()).append("};");
	}

	void emitTs(final StringBuilder sbf) {
		final StringBuilder valBuf = new StringBuilder();
		if (this.fromToPairs != null) {
			for (final FromToPair pair : this.fromToPairs) {
				if (valBuf.length() > 0) {
					valBuf.append(C);
				}
				pair.emitTs(valBuf);
			}
		}

		if (this.exclusivePairs != null) {
			for (final ExclusivePair pair : this.exclusivePairs) {
				if (valBuf.length() > 0) {
					valBuf.append(C);
				}
				pair.emitTs(valBuf);
			}
		}

		if (this.inclusivePairs != null) {
			for (final InclusivePair pair : this.inclusivePairs) {
				if (valBuf.length() > 0) {
					valBuf.append(C);
				}
				pair.emitTs(valBuf);
			}
		}

		if (valBuf.length() > 0) {
			sbf.append("\n\t\tthis.validations = [").append(valBuf).append("];");
		}
	}

	void emitJavaDataClass(final StringBuilder sbf, final String generatedPackage,
			final Map<String, DataType> dataTypes) {
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		final String schemaCls = Util.toClassName(this.name);
		final String cls = schemaCls + "Data";
		String pck = generatedPackage + ".schema";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, SchemaData.class);
		Util.emitImport(sbf, Instant.class);
		Util.emitImport(sbf, LocalDate.class);

		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append(" extends SchemaData {");

		/*
		 * constructor
		 */
		sbf.append("\n\n\tprotected ").append(cls).append("(final ").append(schemaCls)
				.append("Schema schema, final Object[] data) {\n\t\tsuper(schema, data);\n\t}");

		/*
		 * extend the required abstract method getInstance()
		 */
		sbf.append("\n\n\t@Override\n\tprotected ").append(cls).append(" newInstance(final Object[] arr) {");
		sbf.append("\n\t\treturn new ").append(cls).append("((").append(schemaCls)
				.append("Schema) this.schema, arr);\n\t}");

		/*
		 * getters and setters
		 */
		Generator.emitJavaGettersAndSetters(this.fields, sbf, dataTypes);
		sbf.append("\n}\n");
	}

	void emitJavaTableClass(final StringBuilder sbf, final String generatedPackage) {
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		final String schemaCls = Util.toClassName(this.name);
		final String rowCls = schemaCls + "Data";
		final String cls = rowCls + "Table";
		String pck = generatedPackage + ".schema";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, SchemaDataTable.class);

		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents an array of structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append(" extends SchemaDataTable {");

		/*
		 * constructor
		 */
		sbf.append("\n\n\tprotected ").append(cls).append("(final ").append(schemaCls)
				.append("Schema schema, final Object[][] data) {\n\t\tsuper(schema, data);\n\t}");
		/*
		 * override getRow for class-specific return type
		 */
		sbf.append("\n\n\t@Override\n\tpublic ").append(rowCls).append(" getSchemaData(final int idx) {");
		sbf.append("\n\t\treturn(").append(rowCls).append(") super.getSchemaData(idx);\n\t}");

		sbf.append("\n}\n");

	}
}
