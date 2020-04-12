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

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
public class Generator {
	protected static final Logger logger = LoggerFactory.getLogger(Generator.class);
	private static final String FOLDER = "/";

	private static final String EXT_FRM = ".form";
	private static final String EXT_SCH = ".schema";
	private static final String EXT_SQL = ".sql.json";

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (args.length != 5) {
			System.err.println(
					"Usage : java Generator.class resourceRootFolder generatedSourceRootFolder generatedPackageName tsImportPrefix tsOutputFolder");
			return;
		}
		generate(args[0], args[1], args[2], args[3], args[4]);
	}

	/**
	 *
	 * @param inputRootFolder
	 *            folder where application.xlsx file, and spec folder are
	 *            located. e.g.
	 * @param javaRootFolder
	 *            java source folder where the sources are to be generated
	 * @param javaRootPackage
	 *            root
	 * @param tsImportPrefix
	 *            relative path of form folder from the folder where named forms
	 *            are generated.for example ".." in case the two folders are in
	 *            the same parent folder
	 * @param tsRootFolder
	 *            folder where generated ts files are to be saved
	 */
	public static void generate(final String inputRootFolder, final String javaRootFolder, final String javaRootPackage,
			final String tsRootFolder, final String tsImportPrefix) {

		String resourceRootFolder = inputRootFolder;
		if (!inputRootFolder.endsWith(FOLDER)) {
			resourceRootFolder += FOLDER;
		}

		String generatedSourceRootFolder = javaRootFolder;
		if (!generatedSourceRootFolder.endsWith(FOLDER)) {
			generatedSourceRootFolder += FOLDER;
		}
		generatedSourceRootFolder += javaRootPackage.replace('.', '/') + FOLDER;

		/*
		 * create output folders if required
		 */
		if (createOutputFolders(generatedSourceRootFolder,
				new String[] { "schema/", "form/", "list/", "sql/" }) == false) {
			return;
		}

		/*
		 * ts folder
		 */
		if (!ensureFolder(new File(tsRootFolder))) {
			logger.error("Unable to clean/create ts root folder {}", tsRootFolder);
			return;
		}

		final String fileName = resourceRootFolder + Conventions.App.APP_FILE;
		File f = new File(fileName);
		if (f.exists() == false) {
			logger.error("project configuration file {} not found. Aborting..", fileName);
			return;
		}

		final Application app = new Application();
		try (JsonReader reader = new JsonReader(new FileReader(f))) {
			app.fromJson(reader);
		} catch (final Exception e) {
			logger.error("Exception while trying to read file {}. Error: {}", f.getPath(), e.getMessage());
			e.printStackTrace();
			return;
		}

		/*
		 * generate project level components like data types
		 */
		app.emitJava(generatedSourceRootFolder, javaRootPackage, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);

		logger.debug("Going to process schemas under folder {}", resourceRootFolder);
		final Map<String, Schema> schemas = new HashMap<>();
		f = new File(resourceRootFolder + "schema/");
		if (f.exists() == false) {
			logger.error("Schema folder {} not found. No schemas are processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_SCH) == false) {
					logger.debug("Skipping non-schema file {}", fn);
					continue;
				}

				logger.info("file: {}", fn);
				final Schema schema = emitSchema(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app,
						javaRootPackage, tsImportPrefix);
				if (schema != null) {
					schemas.put(schema.name, schema);
				}
			}
		}

		logger.debug("Going to process forms under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_FRM) == false) {
					logger.debug("Skipping non-form file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitForm(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app, javaRootPackage,
						tsImportPrefix, schemas);
			}
		}

		logger.debug("Going to process sqls under folder {}sql/", resourceRootFolder);
		f = new File(resourceRootFolder + "sql/");
		if (f.exists() == false) {
			logger.error("Sql folder {} not found. No sqls processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_SQL) == false) {
					logger.debug("Skipping non-sql file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitSql(file, generatedSourceRootFolder, app.dataTypes, javaRootPackage);
			}
		}
	}

	private static void emitForm(final File file, final String generatedSourceRootFolder, final String tsOutputFolder,
			final DataTypes dataTypes, final Application app, final String rootPackageName, final String tsImportPrefix,
			final Map<String, Schema> schemas) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_FRM.length());
		logger.debug("Going to generate Form " + fn);
		final Form form;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			form = Util.GSON.fromJson(reader, Form.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Form {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		if (!fn.equals(form.name)) {
			logger.error("File {} contains form named {}. It is mandatory to use form name same as the filename", fn,
					form.name);
			return;
		}
		Schema schema = null;
		if (form.schemaName != null) {
			schema = schemas.get(form.schemaName);
			if (schema == null) {
				logger.error("Form {} uses schema {}, but that schema is not defined", form.name, form.schemaName);
				return;
			}
		}
		form.initialize(schema);
		final StringBuilder sbf = new StringBuilder();

		final String cls = Util.toClassName(fn);
		form.emitJavaForm(sbf, rootPackageName);
		final String outPrefix = generatedSourceRootFolder + "form/" + cls;
		Util.writeOut(outPrefix + "Form.java", sbf);

		sbf.setLength(0);
		form.emitJavaFormData(sbf, rootPackageName, app.dataTypes.dataTypes);
		Util.writeOut(outPrefix + "Fd.java", sbf);

		sbf.setLength(0);
		form.emitJavaFormDataTable(sbf, rootPackageName);
		Util.writeOut(outPrefix + "Fdt.java", sbf);

		sbf.setLength(0);
		form.emitTs(sbf, dataTypes.dataTypes, app.valueLists, app.keyedLists, tsImportPrefix);
		Util.writeOut(tsOutputFolder + fn + "Form.ts", sbf);

	}

	/**
	 * @param files
	 * @param generatedSourceRootFolder
	 * @param tsOutputFolder
	 * @param dataTypes
	 * @param app
	 * @param rootPackageName
	 * @param tsImportPrefix
	 */
	private static Schema emitSchema(final File file, final String generatedSourceRootFolder,
			final String tsOutputFolder, final DataTypes dataTypes, final Application app, final String packageName,
			final String tsImportPrefix) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_SCH.length());
		logger.debug("Going to generate schema " + fn);
		final Schema schema;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			schema = Util.GSON.fromJson(reader, Schema.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Schema {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return null;
		}
		if (!fn.equals(schema.name)) {
			logger.error("File {} contains schema named {}. It is mandatory to use schema name same as the filename",
					fn, schema.name);
			return null;
		}

		schema.init();

		final String outNamePrefix = generatedSourceRootFolder + "schema/" + Util.toClassName(fn);
		/*
		 * schema.java
		 */
		final StringBuilder sbf = new StringBuilder();
		schema.emitJavaClass(sbf, packageName);
		String outName = outNamePrefix + "Schema.java";
		Util.writeOut(outName, sbf);

		/*
		 * schemaRow.java
		 */
		sbf.setLength(0);
		schema.emitJavaDataClass(sbf, packageName, dataTypes.dataTypes);
		outName = outNamePrefix + "Data.java";
		Util.writeOut(outName, sbf);

		/*
		 * schemaTable.java
		 */
		sbf.setLength(0);
		schema.emitJavaTableClass(sbf, packageName);
		outName = outNamePrefix + "DataTable.java";
		Util.writeOut(outName, sbf);

		return schema;
	}

	private static void emitSql(final File file, final String generatedSourceRootFolder, final DataTypes dataTypes,
			final String packageName) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_SQL.length());
		logger.debug("Going to generate Sql " + fn);
		final Sql sql;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			sql = Util.GSON.fromJson(reader, Sql.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Sql {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		final String cls = Util.toClassName(fn) + "Sql";
		final StringBuilder sbf = new StringBuilder();
		sql.emitJava(sbf, packageName, cls, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME, dataTypes.dataTypes);
		final String outName = generatedSourceRootFolder + "sql/" + Util.toClassName(fn) + "Sql.java";
		Util.writeOut(outName, sbf);

	}

	private static boolean createOutputFolders(final String root, final String[] folders) {
		boolean allOk = true;
		for (final String folder : folders) {
			if (!ensureFolder(new File(root + folder))) {
				allOk = false;
			}
		}
		return allOk;
	}

	private static boolean ensureFolder(final File f) {
		final String folder = f.getAbsolutePath();
		if (f.exists()) {
			if (f.isDirectory()) {
				logger.debug("All files in folder {} are deleted", folder);
				for (final File ff : f.listFiles()) {
					if (!ff.delete()) {
						logger.error("Unable to delete file {}", ff.getAbsolutePath());
						return false;
					}
				}
				return true;
			}

			if (f.delete()) {
				logger.debug("{} is a file. It is deleted to make way for a directory with the same name", folder);
			} else {
				logger.error("{} is a file. Unable to delete it to create a folder with that name", folder);
				return false;
			}
		}
		if (!f.mkdirs()) {
			logger.error("Unable to create folder {}. Aborting..." + f.getPath());
			return false;
		}
		return true;
	}

	static void emitJavaGettersAndSetters(final Field[] fields, final StringBuilder sbf,
			final Map<String, DataType> dataTypes) {
		for (final Field f : fields) {
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

			sbf.append("\n\n\t/**\n\t * set value for ").append(nam);
			sbf.append("\n\t * @param value to be assigned to ").append(nam);
			sbf.append("\n\t */");
			sbf.append("\n\tpublic void set").append(cls).append('(').append(typ).append(" value){");
			sbf.append("\n\t\tthis.fieldValues[").append(f.index).append("] = value;");
			sbf.append("\n\t}");

			sbf.append("\n\n\t/**\n\t * @return value of ").append(nam).append("\n\t */");
			sbf.append("\n\tpublic ").append(typ).append(" get").append(cls).append("(){");
			sbf.append("\n\t\treturn super.get").append(get).append("Value(").append(f.index).append(");");
			sbf.append("\n\t}");
		}

	}
}
