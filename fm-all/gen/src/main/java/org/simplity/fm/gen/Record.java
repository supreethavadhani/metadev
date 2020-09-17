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
import org.simplity.fm.core.data.DbTable;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.core.validn.ExclusiveValidation;
import org.simplity.fm.core.validn.FromToValidation;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.validn.InclusiveValidation;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents the contents of a spread sheet for a form
 *
 * @author simplity.org
 *
 */
class Record {
	/*
	 * this logger is used by all related classes of form to give the programmer
	 * the right stream of logs to look for any issue in the workbook
	 */
	static final Logger logger = LoggerFactory.getLogger(Record.class);

	private static final String C = ", ";
	private static final String P = "\n\tprivate static final ";

	/*
	 * fields that are read directly from json
	 */
	String name;
	String nameInDb;
	boolean useTimestampCheck;
	boolean generatePage;
	String customValidation;
	String[] operations;
	/*
	 * reason we have it as an array rather than a MAP is that the sequence,
	 * though not recommended, could be hard-coded by some coders
	 */
	Field[] fields;
	FromToPair[] fromToPairs;
	ExclusivePair[] exclusivePairs;
	InclusivePair[] inclusivePairs;

	/*
	 * derived fields required for generating java/ts
	 */
	Map<String, Field> fieldMap;
	Field[] fieldsWithList;
	Field[] keyFields;

	Field tenantField;
	Field timestampField;
	Field generatedKeyField;

	/*
	 * some tables may have primary key, but not have anything to update
	 */
	transient boolean isUpdatable;

	void init(final Map<String, DataType> dataTypes) {
		/*
		 * we want to check for duplicate definition of standard fields
		 */
		Field modifiedAt = null;
		Field modifiedBy = null;
		Field createdBy = null;
		Field createdAt = null;

		this.fieldMap = new HashMap<>();
		final List<Field> list = new ArrayList<>();
		final List<Field> keyList = new ArrayList<>();

		int idx = -1;
		for (final Field field : this.fields) {
			if (field == null) {
				continue;
			}
			field.init(dataTypes);
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

			FieldType ct = field.getFieldType();
			if (ct == null) {
				if (field.dbColumnName == null) {
					logger.warn("{} is not linked to a db-column. No I/O happens on this field.", fieldName);
					continue;
				}
				logger.error(
						"{} is linked to a db-column {} but does not specify a db-column-type. it is treated as an optionl field.",
						fieldName, field.dbColumnName);
				ct = FieldType.OptionalData;
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
			this.fieldsWithList = list.toArray(new Field[0]);
		}

		if (keyList.size() > 0) {
			this.keyFields = keyList.toArray(new Field[0]);
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

	void emitJavaClass(final StringBuilder sbf, final String generatedPackage, final DataTypes dataTypes) {
		final String typesName = Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		/*
		 * our package name is rootPackage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		String pck = generatedPackage + ".rec";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		final boolean isDb = this.nameInDb != null && this.nameInDb.isEmpty() == false;
		/*
		 * imports
		 */
		Util.emitImport(sbf, LocalDate.class);
		Util.emitImport(sbf, Instant.class);
		Util.emitImport(sbf, IInputObject.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.RecordMetaData.class);
		if (isDb) {
			Util.emitImport(sbf, org.simplity.fm.core.data.Dba.class);
			Util.emitImport(sbf, org.simplity.fm.core.data.DbField.class);
			Util.emitImport(sbf, org.simplity.fm.core.data.DbRecord.class);
			Util.emitImport(sbf, FieldType.class);
		} else {
			Util.emitImport(sbf, org.simplity.fm.core.data.Record.class);
		}
		Util.emitImport(sbf, IValidation.class);
		Util.emitImport(sbf, IServiceContext.class);
		Util.emitImport(sbf, List.class);

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
		final String cls = Util.toClassName(this.name) + "Record";

		sbf.append("\n\n/**\n * class that represents structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append(" extends ");
		if (isDb) {
			sbf.append("Db");
		}
		sbf.append("Record {");

		this.emitJavaFields(sbf, typesName, isDb);
		this.emitJavaValidations(sbf);

		sbf.append("\n\n\tprivate static final RecordMetaData META = new RecordMetaData(\"");
		sbf.append(this.name).append("\", FIELDS, VALIDS);");

		if (isDb) {
			this.emitDbSpecific(sbf, cls);
		} else {
			emitNonDbSpecific(sbf, cls);
		}

		/*
		 * newInstane()
		 */
		sbf.append("\n\n\t@Override\n\tpublic ").append(cls).append(" newInstance(final Object[] values) {");
		sbf.append("\n\t\treturn new ").append(cls).append("(values);\n\t}");

		/*
		 * parseTable() override for better type-safety
		 */
		sbf.append("\n\n\t@Override\n\t@SuppressWarnings(\"unchecked\")\n\tpublic List<").append(cls);
		sbf.append(
				"> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {");
		sbf.append("\n\t\treturn (List<").append(cls)
				.append(">) super.parseTable(inputObject, memberName, forInsert, ctx);\n\t}");

		/*
		 * getters and setters
		 */
		Generator.emitJavaGettersAndSetters(this.fields, sbf, dataTypes.dataTypes);
		sbf.append("\n}\n");
	}

	static private void emitNonDbSpecific(final StringBuilder sbf, final String cls) {
		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**  default constructor */");
		sbf.append("\n\tpublic ").append(cls).append("() {\n\t\tsuper(META, null);\n\t}");

		sbf.append("\n\n\t/**\n\t *@param values initial values\n\t */");
		sbf.append("\n\tpublic ").append(cls).append("(Object[] values) {\n\t\tsuper(META, values);\n\t}");
	}

	private void emitDbSpecific(final StringBuilder sbf, final String cls) {
		sbf.append("\n\t/* DB related */");

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

		sbf.append("\n\n\tprivate static final Dba DBA = new Dba(FIELDS, \"").append(this.nameInDb).append("\", ");
		sbf.append("SELECT, SELECT_IDX,");
		if (this.keyFields == null) {
			sbf.append("null, null, null, null, null, null, null");
		} else {
			sbf.append("INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX");
		}
		sbf.append(");");
		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**  default constructor */");
		sbf.append("\n\tpublic ").append(cls).append("() {\n\t\tsuper(DBA, META, null);\n\t}");

		sbf.append("\n\n\t/**\n\t * @param values initial values\n\t */");
		sbf.append("\n\tpublic ").append(cls).append("(Object[] values) {\n\t\tsuper(DBA, META, values);\n\t}");
	}

	private void emitJavaFields(final StringBuilder sbf, final String dataTypesName, final boolean isDb) {
		sbf.append("\n\tprivate static final Field[] FIELDS = ");
		if (this.fields == null) {
			sbf.append("null;");
			return;
		}
		sbf.append("{");
		boolean isFirst = true;
		for (final Field field : this.fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			field.emitJavaCode(sbf, dataTypesName, isDb);
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
			for (final Field field : this.fieldsWithList) {
				if (field.listKey == null) {
					continue;
				}
				final Field f = this.fieldMap.get(field.listKey);
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

	private void makeWhere(final StringBuilder clause, final StringBuilder indexes, final Field[] keys) {
		clause.append(" WHERE ");
		boolean firstOne = true;
		for (final Field field : keys) {
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
		for (final Field field : this.fields) {
			final FieldType ct = field.getFieldType();
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
		for (final Field field : this.fields) {
			final FieldType ct = field.getFieldType();
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
			if (ct == FieldType.ModifiedAt || ct == FieldType.CreatedAt) {
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
		for (final Field field : this.fields) {
			final FieldType ct = field.getFieldType();
			if (ct == null || ct.isUpdated() == false) {
				continue;
			}

			if (firstOne) {
				firstOne = false;
			} else {
				updateBuf.append(C);
			}

			updateBuf.append(field.dbColumnName).append("=");
			if (ct == FieldType.ModifiedAt) {
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

	void emitJavaTableClass(final StringBuilder sbf, final String generatedPackage) {
		/*
		 * table is defined only if this record is a DbRecord
		 */
		if (this.nameInDb == null || this.nameInDb.isEmpty()) {
			return;
		}
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		final String c = Util.toClassName(this.name);
		final String recCls = c + "Record";
		final String cls = c + "Table";
		String pck = generatedPackage + ".rec";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, DbTable.class);

		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents an array of records of ").append(this.name);
		sbf.append("\n */");
		sbf.append("\npublic class ").append(cls).append(" extends DbTable<").append(recCls).append("> {");

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */");
		sbf.append("\n\tpublic ").append(cls).append("() {\n\t\tsuper(new ").append(recCls).append("());\n\t}");

		sbf.append("\n}\n");
	}

	void emitClientForm(final StringBuilder sbf) {
		sbf.append("import { Form } from 'simplity';");
		sbf.append("\nexport const ").append(this.name).append("Form: Form = {");
		sbf.append("\n\tname: '").append(this.name).append("',");
		sbf.append("\n\tvalidOperations: {");
		if (this.operations == null || this.operations.length == 0) {
			logger.warn(
					"No operatins are allowed for record {}. Client app will not be able to use auto-service for this record",
					this.name);
		} else {
			for (final String oper : this.operations) {
				sbf.append("\n\t\t").append(oper).append(": true,");
			}
			sbf.setLength(sbf.length() - 1);
		}
		sbf.append("\n\t},");
		sbf.append("\n\tfields: {");
		for (final Field field : this.fields) {
			field.emitFormTs(sbf);
			sbf.append(',');
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("\n\t}\n}\n");
	}

	void emitListPage(final StringBuilder sbf) {
		//@formatter:off
		sbf.append(
				"import { NavigationAction, Page, FilterAction } from 'simplity';\n" +
				"\n" +
				"export const " + this.name + "List: Page = {\n" +
				"    name: \"" + this.name  + "-list\",\n" +
				"    isEditable: false,\n" +
				"    titlePrefix: '" +this.name.toUpperCase() + " LIST',\n" +
				"    hideMainMenu: false,\n" +
				"    hideSubMenu: false,\n" +
				"    onLoad: 'filter',\n" +
				"    middleButtons: [\n" +
				"        {\n" +
				"            name: 'create-button',\n" +
				"            compType: 'button',\n" +
				"            buttonType: 'primary',\n" +
				"            onClick: 'create',\n" +
				"            label: 'New " + Util.toClassName(this.name)+ "',\n" +
				"            tooltip: 'Add details for a new " + Util.toClassName(this.name)+ "',\n" +
				"        }\n" +
				"    ],\n" +
				"    actions: {\n" +
				"        filter: {\n" +
				"            name: 'filter',\n" +
				"            type: 'form',\n" +
				"            formOperation: 'filter',\n" +
				"            childName: 'itemList',\n" +
				"            formName: '" + this.name + "',\n" +
				"        } as FilterAction,\n" +
				"        create: {\n" +
				"            name: 'create',\n" +
				"            type: 'navigation',\n" +
				"            operation: 'open',\n" +
				"            menuName: '" + this.name + "-add',\n" +
				"        } as NavigationAction,\n" +
				"        edit: {\n" +
				"            name: 'edit',\n" +
				"            type: 'navigation',\n" +
				"            operation: 'open',\n" +
				"            menuName: '" + this.name +"-edit',\n" +
				"            params: { ");
		boolean isFirst = true;
		for(final Field key: this.keyFields) {
			if(isFirst) {
				isFirst = false;
			}else {
				sbf.append(", ");
			}
			sbf.append("\n\t\t\t\t").append( key.name).append(": '$").append(key.name).append("'");
		}
		sbf.append("\n\t\t\t}\n" +
				"        } as NavigationAction,\n" +
				"        cancel: {\n" +
				"            name: 'cancel',\n" +
				"            type: 'navigation',\n" +
				"            operation: 'cancel'\n" +
				"        } as NavigationAction\n" +
				"    },\n" +
				"    dataPanel: {\n" +
				"        name: 'container-panel',\n" +
				"        compType: 'panel',\n" +
				"        panelType: 'container',\n" +
				"        children: [\n" +
				"            {\n" +
				"                name: 'itemList',\n" +
				"                compType: 'table',\n" +
				"                tableType: 'display',\n" +
				"                formName: '" + this.name + "',\n" +
				"                onRowClick: 'edit',\n" +
				"                columns: [");
		isFirst = true;
		for(final Field field : this.fields) {
			if(!field.renderInList) {
				continue;
			}
			if(isFirst) {
				isFirst = false;
			}else {
				sbf.append(',');
			}
			sbf.append(
				"\n                    {" +
				"\n                        name: '" + field.name +"'," +
				"\n                        compType: 'field'," +
				"\n                        fieldType: 'output'" +
				"\n                    }"
			);
		}

		sbf.append(
				"\n                ]\n" +
				"            }\n" +
				"        ]\n" +
				"    }\n" +
				"}");
	}
	//@formatter:on

	void emitSavePage(final StringBuilder sbf) {
		//@formatter:off
		sbf.append(
				"import { Page, EditCard, NavigationAction, SaveAction, GetAction } from 'simplity';\n" +
				"\n" +
				"export const " + this.name + "Save: Page = {\n" +
				"    name: '" + this.name + "-save',\n" +
				"    formName: 'department',\n" +
				"    isEditable: true,\n" +
				"    titlePrefix: 'NEW " + this.name.toUpperCase() + "',\n" +
				"    hideMainMenu: true,\n" +
				"    hideSubMenu: true,\n" +
				"    onLoad: 'get',\n" +
				"    inputIsForUpdate: true,\n" +
				"    inputs: {"
				);
		boolean isFirst = true;
		for(final Field key: this.keyFields) {
			if(isFirst) {
				isFirst = false;
			}else {
				sbf.append(", ");
			}
			sbf.append("\n\t\t").append( key.name).append(": false");
		}

		sbf.append("\n    },\n" +
				"    actions: {\n" +
				"        get: {\n" +
				"            name: 'get',\n" +
				"            type: 'form',\n" +
				"            formOperation: 'get',\n" +
				"            formName: 'department',\n" +
				"            params: {\n" +
				"                departmentId: true\n" +
				"            }\n" +
				"        } as GetAction,\n" +
				"        save: {\n" +
				"            name: 'save',\n" +
				"            type: 'form',\n" +
				"            formOperation: 'save',\n" +
				"            onSuccess: 'close'\n" +
				"        } as SaveAction,\n" +
				"        cancel: {\n" +
				"            name: 'cancel',\n" +
				"            type: 'navigation',\n" +
				"            operation: 'cancel'\n" +
				"        } as NavigationAction,\n" +
				"        close: {\n" +
				"            name: 'close',\n" +
				"            type: 'navigation',\n" +
				"            operation: 'close'\n" +
				"        } as NavigationAction\n" +
				"    },\n" +
				"    dataPanel: {\n" +
				"        name: 'data-panel',\n" +
				"        compType: 'panel',\n" +
				"        panelType: 'edit',\n" +
				"        children: [\n" +
				"            {\n" +
				"                name: 'data-card',\n" +
				"                cardType: 'edit',\n" +
				"                compType: 'card',\n" +
				"                children: ["
				);

		isFirst = true;
		for(final Field field: this.fields) {
			if(!field.renderInSave) {
				continue;
			}

			if(isFirst) {
				isFirst = false;
			}else {
				sbf.append(',');
			}

			String ft;
			if(field.valueType == ValueType.Boolean) {
				ft = "checkbox";
			}else if(field.listName != null) {
				ft = "select";
			}else {
				ft = "text";
			}
			sbf.append(
					"\n                    {" +
					"\n                        name: '" + field.name + "'," +
					"\n                        compType: 'field'," +
					"\n                        fieldType: '" + ft + "'" +
					"\n                    }"
			);
		}

		sbf.append(
				"                ]\n" +
				"            } as EditCard\n" +
				"        ]\n" +
				"    },\n" +
				"    middleButtons: [\n" +
				"        {\n" +
				"            name: 'cancel-button',\n" +
				"            compType: 'button',\n" +
				"            buttonType: 'secondary',\n" +
				"            onClick: 'cancel',\n" +
				"            label: 'Cancel',\n" +
				"            tooltip: 'Abandon Changes'\n" +
				"        },\n" +
				"        {\n" +
				"            name: 'save-button',\n" +
				"            compType: 'button',\n" +
				"            buttonType: 'primary',\n" +
				"            onClick: 'save',\n" +
				"            label: 'Save',\n" +
				"            tooltip: 'save " + this.name + " details',\n" +
				"            enableWhen: 'valid'\n" +
				"        }\n" +
				"    ]\n" +
				"}"
		);

	}

}
