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

import org.simplity.fm.core.Conventions;
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
	private static final String EXT = ".json";

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
	 * @param outputRootFolder
	 *            java source folder where the sources are to be generated
	 * @param rootPackageName
	 *            root
	 * @param tsImportPrefix
	 *            relative path of form folder from the folder where named forms
	 *            are generated.for example ".." in case the two folders are in
	 *            the same parent folder
	 * @param tsOutputFolder
	 *            folder where generated ts files are to be saved
	 */
	public static void generate(final String inputRootFolder, final String outputRootFolder,
			final String rootPackageName, final String tsImportPrefix, final String tsOutputFolder) {

		String resourceRootFolder = inputRootFolder;
		if (!inputRootFolder.endsWith(FOLDER)) {
			resourceRootFolder += FOLDER;
		}

		String generatedSourceRootFolder = outputRootFolder;
		if (!generatedSourceRootFolder.endsWith(FOLDER)) {
			generatedSourceRootFolder += FOLDER;
		}
		generatedSourceRootFolder += rootPackageName.replace('.', '/') + FOLDER;

		/*
		 * create output folders if required
		 */
		final String[] folders = { "schema/", "form/", "ts/", "list/" };
		if (createOutputFolders(generatedSourceRootFolder, folders) == false) {
			return;
		}

		final String fileName = resourceRootFolder + Conventions.App.APP_FILE + EXT;
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
		app.emitJava(generatedSourceRootFolder, rootPackageName, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);

		logger.info("Going to process schemas under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "schema/");
		if (f.exists() == false) {
			logger.error("Schema folder {} not found. No schemas are processed", f.getPath());
			return;
		}

		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT) == false) {
				logger.info("Skipping non-json file {} ", fn);
				continue;
			}

			emitSchema(file, generatedSourceRootFolder, tsOutputFolder, app.dataTypes, app, rootPackageName,
					tsImportPrefix);
		}

		logger.info("Going to process forms under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", f.getPath());
			return;
		}

		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT) == false) {
				logger.info("Skipping non-json file {} " + fn);
				continue;
			}
			emitForm(file, generatedSourceRootFolder, tsOutputFolder, app.dataTypes, app, rootPackageName,
					tsImportPrefix);
		}
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
	private static void emitForm(final File file, final String generatedSourceRootFolder, final String tsOutputFolder,
			final DataTypes dataTypes, final Application app, final String rootPackageName,
			final String tsImportPrefix) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT.length());
		logger.info("Going to generate Form " + fn);
		final Form form;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			form = Util.GSON.fromJson(reader, Form.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Form {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		final StringBuilder sbf = new StringBuilder();
		form.emitJavaForm(sbf, rootPackageName);
		final String outName = generatedSourceRootFolder + "form/" + Util.toClassName(fn) + ".java";
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
	private static void emitSchema(final File file, final String generatedSourceRootFolder, final String tsOutputFolder,
			final DataTypes dataTypes, final Application app, final String packageName, final String tsImportPrefix) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT.length());
		logger.info("Going to generate schema " + fn);
		final Schema schema;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			schema = Util.GSON.fromJson(reader, Schema.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Schema {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		final StringBuilder sbf = new StringBuilder();
		schema.emitJavaClass(sbf, packageName);
		final String outName = generatedSourceRootFolder + "schema/" + Util.toClassName(fn) + ".java";
		Util.writeOut(outName, sbf);

		sbf.setLength(0);
		// schema.emitTs(sbf, typesMap, project.lists, project.keyedLists,
		// tsImportPrefix);
		// outName = tsOutputFolder + fn + ".ts";
		// Util.writeOut(outName, sbf);
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

}
