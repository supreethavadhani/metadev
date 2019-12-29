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
		if (createOutputFolders(generatedSourceRootFolder, new String[] { "schema/", "form/", "list/" }) == false) {
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

		logger.info("Going to process schemas under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "schema/");
		if (f.exists() == false) {
			logger.error("Schema folder {} not found. No schemas are processed", f.getPath());
			return;
		}

		final Map<String, Schema> schemas = new HashMap<>();
		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT_SCH) == false) {
				logger.info("Skipping non-schema file {}", fn);
				continue;
			}

			final Schema schema = emitSchema(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app,
					javaRootPackage, tsImportPrefix);
			if (schema != null) {
				schemas.put(schema.name, schema);
			}
		}

		logger.info("Going to process forms under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", f.getPath());
			return;
		}

		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT_FRM) == false) {
				logger.info("Skipping non-form file {} ", fn);
				continue;
			}
			emitForm(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app, javaRootPackage, tsImportPrefix,
					schemas);
		}
	}

	private static void emitForm(final File file, final String generatedSourceRootFolder, final String tsOutputFolder,
			final DataTypes dataTypes, final Application app, final String rootPackageName, final String tsImportPrefix,
			final Map<String, Schema> schemas) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_FRM.length());
		logger.info("Going to generate Form " + fn);
		final Form form;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			form = Util.GSON.fromJson(reader, Form.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Form {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		if (!fn.equals(form.name)) {
			logger.error("File {} contains form named {}. It is mandatory to use schema name same as the filename", fn,
					form.name);
			return;
		}
		final Schema schema = schemas.get(form.schemaName);
		if (schema == null) {
			logger.error("Form {} uses schema {}, but that schema is not defined", form.name, form.schemaName);
			return;
		}
		form.initialize(schema);
		final StringBuilder sbf = new StringBuilder();

		final String cls = Util.toClassName(fn);
		form.emitJavaForm(sbf, rootPackageName);
		String outName = generatedSourceRootFolder + "form/" + cls + ".java";
		Util.writeOut(outName, sbf);

		sbf.setLength(0);
		form.emitJavaFormData(sbf, rootPackageName, app.dataTypes.dataTypes);
		outName = generatedSourceRootFolder + "form/" + cls + "Data.java";
		Util.writeOut(outName, sbf);

		sbf.setLength(0);
		form.emitJavaFormDataTable(sbf, rootPackageName);
		outName = generatedSourceRootFolder + "form/" + cls + "DataTable.java";
		Util.writeOut(outName, sbf);

		sbf.setLength(0);
		form.emitTs(sbf, dataTypes.dataTypes, app.valueLists, app.keyedLists, tsImportPrefix);
		outName = tsOutputFolder + Util.toClassName(fn) + ".ts";
		Util.writeOut(outName, sbf);

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
		logger.info("Going to generate schema " + fn);
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
		String outName = outNamePrefix + ".java";
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

	private static boolean createOutputFolders(final String root, final String[] folders) {
		boolean allOk = true;
		for (final String folder : folders) {
			final File f = new File(root + folder);
			if (!f.exists()) {
				if (!f.mkdirs()) {
					logger.error("Unable to create folder {}. Aborting..." + f.getPath());
					allOk = false;
				}
			}
		}
		return allOk;
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
