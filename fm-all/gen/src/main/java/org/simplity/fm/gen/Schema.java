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

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.data.DbMetaData;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.core.validn.ExclusiveValidation;
import org.simplity.fm.core.validn.FromToValidation;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.validn.InclusiveValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	static final Logger logger = LoggerFactory.getLogger("Form");

	private static final String C = ", ";
	private static final String EQ = " = ";
	private static final String P = "\n\tprivate static final ";

	/*
	 * fields that are read directly from json
	 */
	String name;
	String dbName;
	boolean useTimeStampCheck;
	String customValidationName;
	/*
	 * reason we have it as an array rather than a MAPis that teh sequence,
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
	String generatedColumnName;

	/*
	 * some tables may have primary key, but not have anything to update
	 */
	transient boolean isUpdatable;

	void init() {
		this.fieldMap = new HashMap<>();

		/*
		 * we want to check for duplicate definition of standard fields
		 */
		DbField modifiedAt = null;
		DbField modifiedBy = null;
		DbField createdBy = null;
		DbField createdAt = null;

		if (this.fields != null) {
			final List<DbField> list = new ArrayList<>();
			final List<DbField> keyList = new ArrayList<>();

			for (final DbField field : this.fields) {
				this.fieldMap.put(field.name, field);
				if (field.listName != null) {
					list.add(field);
				}

				final ColumnType ct = field.columnType;
				if (ct == null) {
					continue;
				}
				field.isRequired = ct.isRequired();
				if (ct == ColumnType.GeneratedPrimaryKey) {
					if (this.generatedColumnName != null) {
						logger.error("ONly one generated key please. Found {} as well as {} as generated primary keys.",
								field.name, keyList.get(0).name);
					} else {
						if (keyList.size() > 0) {
							logger.error(
									"Field {} is marked as a generated primary key. But {} is also marked as a primary key field.",
									field.name, keyList.get(0).name);
							keyList.clear();
						}
						keyList.add(field);
						this.generatedColumnName = field.dbColumnName;
					}
					continue;
				}

				if (ct == ColumnType.PrimaryKey) {
					if (this.generatedColumnName != null) {
						logger.error(
								"{} is defined as a generated primary key, but {} is also defined as a primary key.",
								keyList.get(0).name, field.name);
					} else {
						keyList.add(field);
					}
					continue;
				}

				if (ct == ColumnType.ModifiedAt) {
					if (modifiedAt == null) {
						modifiedAt = field;
						if (this.useTimeStampCheck) {
							this.timestampField = field;
						}
					} else {
						logger.error("{} and {} are both defined as lastModifiedAt!!", field.name,
								this.timestampField.name);
					}
					continue;
				}

				if (ct == ColumnType.TenantKey) {
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
				}

				if (ct == ColumnType.ModifiedBy) {
					if (modifiedBy == null) {
						modifiedBy = field;
					} else {
						logger.error("Only one field to be used as modifiedBy but {} and {} are marked", field.name,
								modifiedBy.name);
					}
				}
				if (ct == ColumnType.CreatedAt) {
					if (createdAt == null) {
						createdAt = field;
					} else {
						logger.error("Only one field to be used as createdAt but {} and {} are marked", field.name,
								createdAt.name);
					}
				}
				if (ct == ColumnType.CreatedBy) {
					if (createdBy == null) {
						createdBy = field;
					} else {
						logger.error("Only one field to be used as createdBy but {} and {} are marked", field.name,
								createdBy.name);
					}
				}
			}

			if (list.size() > 0) {
				this.fieldsWithList = list.toArray(new DbField[0]);
			}

			if (keyList.size() > 0) {
				this.keyFields = keyList.toArray(new DbField[0]);
			}

			if (this.useTimeStampCheck && this.timestampField == null) {
				logger.error(
						"Table is designed to use time-stamp for concurrancy, but no field with columnType=modifiedAt");
				this.useTimeStampCheck = false;
			}
		}
	}

	Set<String> getNameSet() {
		final Set<String> names = new HashSet<>();
		if (this.fields != null) {
			for (final DbField field : this.fields) {
				names.add(field.name);
			}
		}
		return names;
	}

	private static String getQualifier(final String nam) {
		final int idx = nam.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return nam.substring(0, idx);
	}

	void emitJavaClass(final StringBuilder sbf, final String generatedPackage) {
		final String typesName = Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		String pck = getQualifier(this.name);
		String cls = null;
		if (pck == null) {
			pck = generatedPackage + ".form";
			cls = Util.toClassName(this.name);
		} else {
			cls = Util.toClassName(this.generatedColumnName.substring(pck.length() + 1));
			pck = generatedPackage + ".form." + pck;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.DbField.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Schema.class);
		Util.emitImport(sbf, IValidation.class);
		Util.emitImport(sbf, DbMetaData.class);
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
		sbf.append("\nimport ").append(generatedPackage).append('.').append(typesName).append(';');
		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append(" extends Form {");

		/*
		 * all fields and child forms indexes are available as constants
		 */
		this.emitJavaConstants(sbf);
		this.emitDbStuff(sbf);

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**\n\t *\n\t */");
		sbf.append("\n\tpublic ").append(cls).append("() {");
		sbf.append("\n\t\tthis.uniqueName = \"").append(this.name).append("\";");

		this.emitJavaFields(sbf, typesName);

		this.emitJavaValidations(sbf);

		sbf.append("\n\n\t\tthis.setDbMeta();");
		sbf.append("\n\t\tthis.initialize();");

		sbf.append("\n\t}\n}\n");
	}

	private void emitDbStuff(final StringBuilder sbf) {
		if (this.dbName == null || this.dbName.isEmpty()) {
			logger.warn("dbName not set. no db related code generated for this form");
			sbf.append("\n\n\tprivate void setDbMeta(){\n\t\t//\n\t}");
			return;
		}

		this.emitSelect(sbf);
		if (this.keyFields == null) {
			logger.info(
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
			sbf.append(P).append("String DELETE = \"DELETE FROM ").append(this.dbName).append("\";");
		}

		sbf.append("\n\n\tprivate void setDbMeta(){");
		final String t = "\n\t\tm.";
		sbf.append("\n\t\tDbMetaData m = new DbMetaData();");

		sbf.append(t).append("selectClause = SELECT;");
		if (this.keyFields != null) {

			sbf.append(t).append("selectParams = this.getParams(SELECT_IDX);");
			sbf.append(t).append("insertClause = INSERT;");
			sbf.append(t).append("insertParams = this.getParams(INSERT_IDX);");

			sbf.append(t).append("whereClause = WHERE;");
			sbf.append(t).append("whereParams = this.getParams(WHERE_IDX);");
			if (this.isUpdatable) {
				sbf.append(t).append("updateClause = UPDATE;");
				sbf.append(t).append("updateParams = this.getParams(UPDATE_IDX);");
			}
			sbf.append(t).append("deleteClause = DELETE;");
			if (this.generatedColumnName != null) {
				sbf.append(t).append("generatedColumnName = \"").append(this.generatedColumnName).append("\";");
			}
		}

		if (this.useTimeStampCheck) {
			sbf.append(t).append("timestampField = this.fields[").append(this.timestampField.index).append("];");
		}

		if (this.tenantField != null) {
			sbf.append(t).append("tenantField = this.fields[").append(this.tenantField.index).append("];");
		}

		sbf.append("\n\t\tthis.dbMetaData = m;");
		sbf.append("\n\t}");
	}

	private void emitJavaConstants(final StringBuilder sbf) {
		for (final DbField field : this.fields) {
			sbf.append("\n\tpublic static final int ").append(field.name).append(EQ).append(field.index).append(';');
		}
	}

	private void emitJavaFields(final StringBuilder sbf, final String dataTypesName) {
		if (this.fields == null) {
			sbf.append("\n\t\tthis.fields = null;");
			return;
		}
		sbf.append("\n\n\t\tField[] flds = {");
		boolean isFirst = true;
		for (final DbField field : this.fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			field.emitJavaCode(sbf, dataTypesName);
		}
		sbf.append("\n\t\t};\n\t\tthis.fields = flds;");
	}

	private void emitJavaValidations(final StringBuilder sbf) {
		sbf.append("\n\n\t\tIValidation[] vlds = {");
		final int n = sbf.length();
		final String sufix = ",\n\t\t\t";
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
					logger.error("DbField {} specifies {} as listKey, but that field is not defined");
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

		sbf.append("};");
		sbf.append("\n\t\tthis.validations = vlds;");
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
			final ColumnType ct = field.columnType;
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

		sbf.append(" FROM ").append(this.dbName);
		sbf.append("\";");
		sbf.append(P).append("int[] SELECT_IDX = {").append(idxSbf).append("};");

	}

	private void emitInsert(final StringBuilder sbf) {
		sbf.append(P).append(" String INSERT = \"INSERT INTO ").append(this.dbName).append('(');
		final StringBuilder idxSbf = new StringBuilder();
		idxSbf.append(P).append("int[] INSERT_IDX = {");
		final StringBuilder vbf = new StringBuilder();
		boolean firstOne = true;
		boolean firstField = true;
		for (final DbField field : this.fields) {
			final ColumnType ct = field.columnType;
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
		updateBuf.append(P).append(" String UPDATE = \"UPDATE ").append(this.dbName).append(" SET ");
		final StringBuilder idxBuf = new StringBuilder();
		idxBuf.append(P).append(" int[] UPDATE_IDX = {");
		boolean firstOne = true;
		boolean firstField = true;
		for (final DbField field : this.fields) {
			final ColumnType ct = field.columnType;
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
		idxBuf.append(C).append(whereIndexes);
		updateBuf.append(whereClause);

		if (this.useTimeStampCheck) {
			updateBuf.append(" AND ").append(this.timestampField.dbColumnName).append("=?");
			idxBuf.append(C).append(this.timestampField.index);
		}
		updateBuf.append("\";");
		sbf.append(updateBuf.toString()).append(idxBuf.toString()).append("};");
	}

	public static void main(final String[] args) throws Exception {
		final String file = "c:/repos/forms/fm-all/gen/src/test/resources/schema.json";
		try (Reader reader = new FileReader(file)) {
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final Schema schema = gson.fromJson(reader, Schema.class);
			System.out.print(gson.toJson(schema));
		}
	}
}
