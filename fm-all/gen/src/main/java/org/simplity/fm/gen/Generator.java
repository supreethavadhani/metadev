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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.form.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Generator {

	protected static final Logger logger = LoggerFactory.getLogger(Generator.class);
	private static final String[] PROJECT_SHEET_NAMES = { "dataTypes", "valueLists", "keyedValueLists", "runtimeLists",
			"commonFields" };
	private static final String[] SHEET_NAMES = { "specialInstructions", "fields", "childForms", "fromToPairs",
			"mutuallyExclusivePairs", "mutuallyInclusivepairs", "customValidations" };
	private static final String[] SHEET_DESC = { "service or processing ", "fields", "child Forms (tables, sub-forms)",
			"from-To inter-field validations", "either-or type of inter-field validaitons",
			"if-a-then-b type of inter-field validaitons", "custom validations" };
	private static final String EXT = ".xlsx";
	private static final int NBR_CELLS_INCL = 4;
	private static final int NBR_CELLS_FROM_TO = 4;
	private static final int NBR_CELLS_FIELD = 14;
	private static final int NBR_CELLS_EXCL = 4;
	private static final int NBR_CELLS_CHILD_FORM = 10;
	private static final int NBR_CELLS_LIST = 3;
	private static final int NBR_CELLS_KEYED_LIST = 4;
	private static final int NBR_CELLS_DATA_TYPES = 12;
	private static final int NBR_CELLS_RUNTIME_LIST = 6;
	private static final String FOLDER = "/";

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println(
					"Usage : java Generator.class resourceRootFolder generatedSourceRootFolder generatedPackageName");
			return;
		}
		generate(args[0], args[1], args[2]);
	}

	/**
	 * 
	 * @param inputRootFolder folder where application.xlsx file, and spec folder are located. e.g.
	 * @param outputRootFolder java source folder where the sources are to be generated
	 * @param rootPackageName root
	 */
	public static void generate(String inputRootFolder, String outputRootFolder,
			String rootPackageName) {

		String resourceRootFolder = inputRootFolder;
		if(!inputRootFolder.endsWith(FOLDER)) {
			resourceRootFolder += FOLDER;
		}
		
		String generatedSourceRootFolder = outputRootFolder;
		if(!generatedSourceRootFolder.endsWith(FOLDER)) {
			generatedSourceRootFolder += FOLDER;
		}
		generatedSourceRootFolder += rootPackageName.replace('.', '/') + FOLDER;
		
		/*
		 * create output folders if required
		 */
		String[] folders = { "form/", "ts/", "list/", "klist/" };
		if (createOutputFolders(generatedSourceRootFolder, folders) == false) {
			return;
		}

		String fileName = resourceRootFolder + Conventions.App.APP_FILE;
		File f = new File(fileName);
		if (f.exists() == false) {
			logger.error("project configuration file {} not found. Aborting..", fileName);
			return;
		}

		AppComps project = null;
		try (InputStream ins = new FileInputStream(f); Workbook book = new XSSFWorkbook(ins)) {
			int n = book.getNumberOfSheets();
			if (n == 0) {
				logger.error("Project Work book {} has no sheets in it. Quitting..", f.getPath());
				return;
			}
			project = parseAppComps(book);

		} catch (Exception e) {
			logger.error("Exception while trying to read workbook {}. Error: {}", f.getPath(), e.getMessage());
			e.printStackTrace();
			return;
		}

		/*
		 * generate project level components like data types
		 */
		project.emitJava(generatedSourceRootFolder, rootPackageName,
				Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);

		logger.info("Going to process forms under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", f.getPath());
			return;
		}

		Map<String, DataType> typesMap = project.getTypes();
		for (File xls : f.listFiles()) {
			emitForm(xls, generatedSourceRootFolder, typesMap, project, rootPackageName);
		}
	}

	private static boolean createOutputFolders(String root, String[] folders) {
		boolean allOk = true;
		for (String folder : folders) {
			File f = new File(root + folder);
			if (!f.exists()) {
				if (!f.mkdirs()) {
					logger.error("Unable to create folder {}. Aborting..." + f.getPath());
					allOk = false;
				}
			}
		}
		return allOk;
	}

	private static void emitForm(File xls, String outputRoot, Map<String, DataType> typesMap, AppComps project, String rootPackageName) {
		String fn = xls.getName();
		if (fn.endsWith(EXT) == false) {
			logger.info("Skipping non-xlsx file {} " + fn);
			return;
		}

		fn = fn.substring(0, fn.length() - EXT.length());
		logger.info("Going to generate form " + fn);
		Form form = null;
		try (Workbook book = new XSSFWorkbook(new FileInputStream(xls))) {
			form = parseForm(book, fn, project.commonFields);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Form {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		StringBuilder sbf = new StringBuilder();
		String fileName = xls.getPath().replace('\\', '/');
		form.emitJavaClass(sbf, fileName, rootPackageName );
		String outName = outputRoot + "form/" + Util.toClassName(fn) + ".java";
		Util.writeOut(outName, sbf);

		if (form.isForDbOnly) {
			return;
		}

		sbf.setLength(0);
		form.emitTs(sbf, typesMap, project.lists, project.keyedLists, fileName);
		outName = outputRoot + "ts/" + fn + ".ts";
		Util.writeOut(outName, sbf);
	}

	/*
	 * Load from XLSX
	 */
	static AppComps parseAppComps(Workbook book) {
		Sheet[] sheets = XlsUtil.readSheets(book, PROJECT_SHEET_NAMES);
		AppComps dt = new AppComps();
		dt.dataTypes = parseTypes(sheets[0]);
		dt.lists = parseLists(sheets[1]);
		dt.keyedLists = parseKeyedLists(sheets[2]);
		dt.runtimeLists = parseRuntimeLists(sheets[3]);
		dt.commonFields = parseCommonFields(sheets[4]);
		return dt;
	}

	private static DataType[] parseTypes(Sheet sheet) {
		logger.info("Started parsing for data types from sheet {} with {} rows ", sheet.getSheetName(),
				(sheet.getLastRowNum() - sheet.getFirstRowNum() + 1));
		List<DataType> typeList = new ArrayList<>();
		XlsUtil.consumeRows(sheet, NBR_CELLS_DATA_TYPES, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				DataType dt = parseDataType(row);
				if (dt != null) {
					typeList.add(dt);
				}
			}
		});

		int n = typeList.size();
		if (n == 0) {
			logger.error("No valid data type parsed!!");
			return null;
		}
		logger.info("{} data types parsed.", n);
		return typeList.toArray(new DataType[0]);
	}

	private static Map<String, ValueList> parseLists(Sheet sheet) {
		// we iterate up to a non-existing row to trigger build
		int n = sheet.getLastRowNum() + 1;
		logger.info("Started parsing for values lists. ");
		ValueListBuilder builder = new ValueListBuilder();
		XlsUtil.consumeRows(sheet, NBR_CELLS_LIST, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				builder.addRow(row);
			}
		});
		/**
		 * signal to the builder to build teh last one that was still being
		 * built
		 */
		Map<String, ValueList> map = builder.done();
		n = map.size();
		if (n == 0) {
			logger.info("No value lists added.");
			return null;
		}
		logger.info("{} value lists added.", n);
		return map;
	}

	private static Map<String, KeyedValueList> parseKeyedLists(Sheet sheet) {
		// we iterate up to a non-existing row to trigger build
		int n = sheet.getLastRowNum() + 1;
		logger.info("Started parsing keyed lists ");
		KeyedValueBuilder builder = new KeyedValueBuilder();
		XlsUtil.consumeRows(sheet, NBR_CELLS_KEYED_LIST, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				builder.addRow(row);
			}
		});
		Map<String, KeyedValueList> map = builder.done();
		n = map.size();
		if (n == 0) {
			logger.info("No keyed value lists added.");
			return null;
		}
		logger.info("{} keyed value lists added.", n);
		return map;
	}

	private static Map<String, RuntimeList> parseRuntimeLists(Sheet sheet) {
		Map<String, RuntimeList> list = new HashMap<>();
		logger.info("Started parsing runtime lists");
		XlsUtil.consumeRows(sheet, NBR_CELLS_RUNTIME_LIST, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				RuntimeList rl = new RuntimeList();
				rl.name = XlsUtil.textValueOf(row.getCell(0));
				rl.table = XlsUtil.textValueOf(row.getCell(1));
				rl.col1 = XlsUtil.textValueOf(row.getCell(2));
				rl.col2 = XlsUtil.textValueOf(row.getCell(3));
				rl.key = XlsUtil.textValueOf(row.getCell(4));
				rl.keyIsNumeric = XlsUtil.boolValueOf(row.getCell(5));
				list.put(rl.name, rl);
			}
		});
		int n = list.size();
		if (n == 0) {
			logger.warn("No runtime lists parsed..");
			return null;
		}
		logger.info("{} runtime list parsed. ", n);
		return list;
	}

	private static Field[] parseCommonFields(Sheet sheet) {
		List<Field> fields = new ArrayList<Field>();
		logger.info("Started parsing common fields");
		XlsUtil.consumeRows(sheet, NBR_CELLS_FIELD, new Consumer<Row>() {
			private int idx = 0;

			@Override
			public void accept(Row row) {
				Field field = parseField(row, this.idx);
				if (field != null) {
					fields.add(field);
					this.idx++;
				}
			}
		});
		int n = fields.size();
		if (n == 0) {
			logger.warn("No common fields parsed..");
			return null;
		}
		logger.info("{} common fields parsed. These fields canbe included in any form with a directive.", n);
		return fields.toArray(new Field[0]);
	}

	static void parseSi(Sheet sheet, Map<String, Object> settings) {
		XlsUtil.consumeRows(sheet, 2, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				String key = XlsUtil.textValueOf(row.getCell(0));
				Object val = XlsUtil.objectValueOf(row.getCell(1));
				logger.info("{}={}", key, val);
				if (key != null || val != null) {
					settings.put(key, val);
				}
			}
		});
	}

	/**
	 * using a builder to accumulate rows for a list and then create a list
	 * 
	 */
	static class KeyedValueBuilder {
		Map<String, KeyedValueList> klists = new HashMap<>();
		private String name = null;
		private String keyName = null;
		private Map<String, Pair[]> lists = new HashMap<>();
		private List<Pair> pairs = new ArrayList<>();

		protected KeyedValueBuilder() {
			//
		}

		Map<String, KeyedValueList> done() {
			this.build();
			Map<String, KeyedValueList> result = this.klists;
			this.newList(null, null);
			this.klists = new HashMap<>();
			return result;
		}

		/**
		 * add row to the builder.
		 * 
		 * @param row
		 */
		void addRow(Row row) {
			String newName = XlsUtil.textValueOf(row.getCell(0));
			String newKey = XlsUtil.textValueOf(row.getCell(1));
			Object val = XlsUtil.objectValueOf(row.getCell(2));
			String label = XlsUtil.textValueOf(row.getCell(3));
			if (this.name == null) {
				/*
				 * this is the very first row being read.
				 */
				if (newName == null) {
					AppComps.logger.error("name of the list not mentioned? row {} skipped...", row.getRowNum());
					return;
				}
				this.newList(newName, newKey);
			} else if (newName != null && newName.equals(this.name) == false) {
				/*
				 * this row is for the next list. build the previous one.
				 */
				this.build();
				this.newList(newName, newKey);
			} else if (newKey != null && newKey.contentEquals(this.keyName) == false) {
				this.addList(newKey);
			}

			this.pairs.add(new Pair(label, val));
		}

		private void newList(String newName, String newKey) {
			this.pairs.clear();
			this.lists = new HashMap<>();
			this.keyName = newKey;
			this.name = newName;
		}

		private void addList(String newKey) {
			if (this.keyName == null || this.pairs.size() == 0) {
				AppComps.logger.error("empty line in lists??, valueList not created.");
			} else {
				this.lists.put(this.keyName, this.pairs.toArray(new Pair[0]));
				this.pairs.clear();
			}
			this.keyName = newKey;
		}

		private void build() {
			if (this.name == null) {
				AppComps.logger.error("empty line in lists??, valueList not created.");
				return;
			}
			this.addList(null);
			this.klists.put(this.name, new KeyedValueList(this.name, this.lists));
			AppComps.logger.info("Keyed value list {} parsed and added with {} keys.", this.name, this.lists.size());
		}
	}

	static InclusivePair[] parseInclusivePairs(Sheet sheet, Map<String, Field> fields) {
		List<InclusivePair> list = new ArrayList<>();
		XlsUtil.consumeRows(sheet, NBR_CELLS_INCL, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				InclusivePair pair = parseInclusivePair(row, fields);
				if (pair != null) {
					list.add(pair);
				}
			}
		});

		if (list.size() == 0) {
			return null;
		}
		return list.toArray(new InclusivePair[0]);
	}

	/**
	 * @param row
	 * @param fields
	 * @return
	 */
	protected static InclusivePair parseInclusivePair(Row row, Map<String, Field> fields) {
		InclusivePair p = new InclusivePair();
		String s1 = XlsUtil.textValueOf(row.getCell(0));
		String s2 = XlsUtil.textValueOf(row.getCell(1));
		if (s1 == null || s2 == null) {
			Form.logger.error("Row {} has missing column value/s. Skipped", row.getRowNum());
			return null;
		}

		Field f1 = fields.get(s1);
		if (f1 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s1, row.getRowNum());
			return null;
		}
		p.index1 = f1.index;

		Field f2 = fields.get(s2);
		if (f2 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s2, row.getRowNum());
			return null;
		}
		p.index2 = f2.index;

		p.value1 = XlsUtil.textValueOf(row.getCell(2));
		p.errorId = XlsUtil.textValueOf(row.getCell(3));
		p.field1 = s1;
		p.field2 = s2;
		p.fieldName = s1;
		return p;
	}

	static FromToPair[] parseFromToPairs(Sheet sheet, Map<String, Field> fields) {
		List<FromToPair> list = new ArrayList<>();
		XlsUtil.consumeRows(sheet, NBR_CELLS_FROM_TO, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				FromToPair pair = parseFromToPair(row, fields);
				if (pair != null) {
					list.add(pair);
				}
			}
		});

		if (list.size() == 0) {
			return null;
		}
		return list.toArray(new FromToPair[0]);
	}

	/**
	 * @param row
	 * @param fields
	 * @return
	 */
	protected static FromToPair parseFromToPair(Row row, Map<String, Field> fields) {
		FromToPair p = new FromToPair();
		String s1 = XlsUtil.textValueOf(row.getCell(0));
		String s2 = XlsUtil.textValueOf(row.getCell(1));
		if (s1 == null || s2 == null) {
			Form.logger.error("Row {} has missing column value/s. Skipped", row.getRowNum());
			return null;
		}
		Field f1 = fields.get(s1);
		if (f1 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s1, row.getRowNum());
			return null;
		}
		p.index1 = f1.index;

		Field f2 = fields.get(s2);
		if (f2 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s2, row.getRowNum());
			return null;
		}
		p.index2 = f2.index;

		p.equalOk = XlsUtil.boolValueOf(row.getCell(2));
		p.fieldName = s1;
		p.errorId = XlsUtil.textValueOf(row.getCell(3));
		p.field1 = s1;
		p.field2 = s2;
		return p;
	}

	static Form parseForm(Workbook book, String formName, Field[] commonFields) {
		logger.info("Started parsing work book " + formName);
		Sheet fieldsSheet = book.getSheet("fields");
		if (fieldsSheet == null) {
			fieldsSheet = book.getSheet("columns");
			if (fieldsSheet == null) {
				logger.error("Work book has neither fields, nor columns. ignored.");
				return null;
			}
			return parseDbForm(book, formName);
		}

		Form form = new Form();
		form.name = formName;
		Sheet[] sheets = new Sheet[SHEET_NAMES.length];
		Sheet sheet;
		for (int i = 0; i < sheets.length; i++) {
			sheet = book.getSheet(SHEET_NAMES[i]);
			if (sheet == null) {
				logger.error("Sheet {} is missing. No {} will be parsed.", SHEET_NAMES[i], SHEET_DESC[i]);
			} else {
				sheets[i] = sheet;
			}
		}

		/*
		 * special instructions
		 */
		sheet = sheets[0];
		boolean addCommonFields = false;
		if (sheet != null) {
			parseSi(sheet, form.params);
			Object obj = form.params.get("addCommonFields");
			if (obj != null && obj instanceof Boolean) {
				addCommonFields = (Boolean) obj;
			}
		}

		/*
		 * fields
		 */
		sheet = sheets[1];
		if (sheet != null) {
			if (addCommonFields) {
				form.fields = parseFields(sheet, commonFields);
			} else {
				form.fields = parseFields(sheet, null);
			}
		}

		Set<String> names = form.getNameSet();
		sheet = sheets[2];
		if (sheet != null) {
			form.childForms = parseChildForms(sheet, names);
		}

		/*
		 * we need a map of fields for cross-reference checks
		 */
		form.buildFieldMap();
		sheet = sheets[3];
		if (sheet != null) {
			form.fromToPairs = parseFromToPairs(sheet, form.fieldMap);
		}

		sheet = sheets[4];
		if (sheet != null) {
			form.exclusivePairs = parseExclusivePairs(sheet, form.fieldMap);
		}

		sheet = sheets[5];
		if (sheet != null) {
			form.inclusivePairs = parseInclusivePairs(sheet, form.fieldMap);
		}

		sheet = sheets[6];
		if (sheet != null) {
			if (XlsUtil.hasContent(sheet.getRow(1), 2)) {
				form.hasCustomValidations = true;
				logger.info("custom validiton added. Ensure that you write the desired java class for this.");
			} else {
				logger.info("No custom validaitons added.");
			}
		}

		logger.info("Done parsing form " + formName);
		return form;
	}

	/**
	 * @param book
	 * @param formName
	 * @return
	 */
	private static Form parseDbForm(Workbook book, String formName) {
		Sheet sheet = book.getSheet("params");
		if (sheet == null) {
			logger.error("Form {} has params sheet missing.");
			return null;
		}
		Form form = new Form();
		form.name = formName;
		parseSi(sheet, form.params);
		if (form.params.get("dbTableName") == null) {
			logger.error("dbTableName is required in params sheet");
			return null;
		}

		/*
		 * db columns
		 */
		sheet = book.getSheet("columns");
		List<Field> list = new ArrayList<>();
		Set<String> fieldNames = new HashSet<>();

		XlsUtil.consumeRows(sheet, 4, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				Field f = new Field();
				f.name = XlsUtil.textValueOf(row.getCell(0));
				f.dbColumnName = XlsUtil.textValueOf(row.getCell(1));
				f.dataType = XlsUtil.textValueOf(row.getCell(2));
				f.columnType = parseColumnType(row.getCell(3));
				f.index = list.size();
				if (fieldNames.add(f.name) == false) {
					logger.error("Field name {} is duplicate at row {}. skipped", f.name, row.getRowNum());
				}
				list.add(f);
			}
		});
		int n = list.size();
		if (n == 0) {
			logger.warn("No fields for this form!!");
			return null;
		}
		form.fields = list.toArray(new Field[0]);
		form.isForDbOnly = true;
		/*
		 * child forms
		 */
		sheet = book.getSheet("children");
		if (sheet == null) {
			return form;
		}

		List<ChildForm> children = new ArrayList<>();
		XlsUtil.consumeRows(sheet, 4, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				ChildForm f = new ChildForm();
				f.formName = XlsUtil.textValueOf(row.getCell(0));
				f.linkParentFields = splitToArray(XlsUtil.textValueOf(row.getCell(1)));
				f.linkChildFields = splitToArray(XlsUtil.textValueOf(row.getCell(2)));
				f.index = list.size();
				children.add(f);
			}
		});
		n = list.size();
		if (n == 0) {
			logger.warn("No children added!!");
		} else {
			form.childForms = children.toArray(new ChildForm[0]);
		}
		return form;
	}

	static Field[] parseFields(Sheet sheet, Field[] commonFields) {
		List<Field> list = new ArrayList<>();
		Set<String> fieldNames = new HashSet<>();
		if (commonFields != null) {
			for (Field field : commonFields) {
				list.add(field);
				fieldNames.add(field.name);
			}
			logger.info("{} common fields added to the form", commonFields.length);
		}
		XlsUtil.consumeRows(sheet, NBR_CELLS_FIELD, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				Field field = parseField(row, list.size());
				if (field == null) {
					return;
				}
				if (fieldNames.add(field.name)) {
					list.add(field);
				} else {
					logger.error("Field name {} is duplicate at row {}. skipped", field.name, row.getRowNum());
				}
			}
		});

		int n = list.size();
		if (n == 0) {
			logger.warn("No fields for this form!!");
			return null;
		}
		return list.toArray(new Field[0]);
	}

	static Field parseField(Row row, int index) {
		Field f = new Field();
		f.name = XlsUtil.textValueOf(row.getCell(0));
		if (f.name == null) {
			logger.error("Name is null in row {}. Row is skipped", row.getRowNum());
			return null;
		}
		f.label = XlsUtil.textValueOf(row.getCell(1));
		f.altLabel = XlsUtil.textValueOf(row.getCell(2));
		// 3 is description. we are not parsing that.
		f.placeHolder = XlsUtil.textValueOf(row.getCell(4));
		f.dataType = XlsUtil.textValueOf(row.getCell(5));
		f.defaultValue = XlsUtil.textValueOf(row.getCell(6));
		f.errorId = XlsUtil.textValueOf(row.getCell(7));
		f.isRequired = XlsUtil.boolValueOf(row.getCell(8));
		f.isEditable = XlsUtil.boolValueOf(row.getCell(9));
		/*
		 * current project has an issue people not managing it properly..
		 */
		f.listName = XlsUtil.textValueOf(row.getCell(10));
		f.listKey = XlsUtil.textValueOf(row.getCell(11));
		f.dbColumnName = XlsUtil.textValueOf(row.getCell(12));
		f.columnType = parseColumnType(row.getCell(13));
		if (f.dbColumnName != null && f.columnType == null) {
			logger.error("Field {} is mapped to db column {} but columnType is not specified. columnName dropped.",
					f.name, f.dbColumnName);
			f.dbColumnName = null;
		} else if (f.dbColumnName == null && f.columnType != null) {
			logger.error("Field {} has columnType but dbColumnName is not specified.", f.name, f.columnType);
			f.columnType = null;
		}
		f.index = index;

		return f;
	}

	static ExclusivePair[] parseExclusivePairs(Sheet sheet, Map<String, Field> fields) {
		List<ExclusivePair> list = new ArrayList<>();
		XlsUtil.consumeRows(sheet, NBR_CELLS_EXCL, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				ExclusivePair pair = parseExclusivePair(row, fields);
				if (pair != null) {
					list.add(pair);
				}
			}
		});

		int n = list.size();
		if (n == 0) {
			Form.logger.info("No either-or inter-field validaiton defined");
			return null;
		}
		Form.logger.info("{}  either-or inter-field validaiton defined", n);
		return list.toArray(new ExclusivePair[0]);
	}

	/**
	 * @param row
	 * @param fields
	 * @return
	 */
	protected static ExclusivePair parseExclusivePair(Row row, Map<String, Field> fields) {
		ExclusivePair p = new ExclusivePair();
		String s1 = XlsUtil.textValueOf(row.getCell(0));
		String s2 = XlsUtil.textValueOf(row.getCell(1));
		if (s1 == null || s2 == null) {
			Form.logger.error("Row {} has missing column value/s. Skipped", row.getRowNum());
			return null;
		}
		Field f1 = fields.get(s1);
		if (f1 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s1, row.getRowNum());
			return null;
		}
		p.index1 = f1.index;

		Field f2 = fields.get(s2);
		if (f2 == null) {
			Form.logger.error("{} is not a field name in this form. row {} skipped", s2, row.getRowNum());
			return null;
		}
		p.index2 = f2.index;

		p.isRequired = XlsUtil.boolValueOf(row.getCell(2));
		p.fieldName = s1;
		p.errorId = XlsUtil.textValueOf(row.getCell(3));
		p.field1 = s1;
		p.field2 = s2;
		return p;
	}

	static DataType parseDataType(Row row) {
		DataType dt = new DataType();
		dt.name = XlsUtil.textValueOf(row.getCell(0));
		if (dt.name == null) {
			AppComps.logger.error("Field name is empty. row {} skipped", row.getRowNum());
			return null;
		}
		String s = XlsUtil.textValueOf(row.getCell(1)).toUpperCase();
		try {
			dt.valueType = ValueType.valueOf(s);
		} catch (Exception e) {
			AppComps.logger.error("{} is not a valid data type. row {} skipped", s, row.getRowNum());
			return null;
		}

		dt.errorId = XlsUtil.textValueOf(row.getCell(2));
		dt.regexDesc = XlsUtil.textValueOf(row.getCell(3));
		dt.regex = XlsUtil.textValueOf(row.getCell(4));
		dt.minLength = (int) XlsUtil.longValueOf(row.getCell(5));
		dt.maxLength = (int) XlsUtil.longValueOf(row.getCell(6));
		dt.minValue = XlsUtil.longValueOf(row.getCell(7));
		dt.maxValue = XlsUtil.longValueOf(row.getCell(8));
		dt.trueLabel = XlsUtil.textValueOf(row.getCell(9));
		dt.falseLabel = XlsUtil.textValueOf(row.getCell(10));
		dt.nbrFractions = (int) XlsUtil.longValueOf(row.getCell(11));
		return dt;
	}

	static ChildForm[] parseChildForms(Sheet sheet, Set<String> names) {
		List<ChildForm> list = new ArrayList<>();
		XlsUtil.consumeRows(sheet, NBR_CELLS_CHILD_FORM, new Consumer<Row>() {

			@Override
			public void accept(Row row) {
				ChildForm child = parseChildForm(row);
				if (child == null) {
					return;
				}
				if (names.add(child.name)) {
					list.add(child);
				} else {
					Form.logger.error("Child form name {} is duplicate at row {}. skipped", child.name,
							row.getRowNum());
				}
			}
		});
		int n = list.size();
		if (n == 0) {
			Form.logger.info("No child forms parsed");
			return null;
		}
		ChildForm[] arr = new ChildForm[n];
		for (int i = 0; i < arr.length; i++) {
			ChildForm child = list.get(i);
			child.index = i;
			arr[i] = child;
		}
		Form.logger.info("{} child forms parsed and added.", n);
		return arr;
	}

	static ChildForm parseChildForm(Row row) {
		ChildForm t = new ChildForm();
		t.name = XlsUtil.textValueOf(row.getCell(0));
		if (t.name == null) {
			Form.logger.error("Name missing in row {}. Skipped", row.getRowNum());
			return null;
		}
		t.label = XlsUtil.textValueOf(row.getCell(1));
		t.formName = XlsUtil.textValueOf(row.getCell(2));
		if (t.formName == null) {
			Form.logger.error("formName is a MUST for a childForm. It is missing in row {}. Row kkipped",
					row.getRowNum());
			return null;
		}
		t.isTabular = XlsUtil.boolValueOf(row.getCell(3));
		t.minRows = (int) XlsUtil.longValueOf(row.getCell(4));
		t.maxRows = (int) XlsUtil.longValueOf(row.getCell(5));
		t.errorId = XlsUtil.textValueOf(row.getCell(6));
		String txt1 = XlsUtil.textValueOf(row.getCell(7));
		String txt2 = XlsUtil.textValueOf(row.getCell(8));

		if (txt1 == null) {
			if (txt2 != null) {
				Form.logger.error("Child form has specified parent link fields, but not child link fields. Ignored");
			}
			return t;
		}

		if (txt2 == null) {
			Form.logger.error("Child form has specified child link fields, but not prent link fields. Ignored");
			return t;
		}
		t.linkParentFields = splitToArray(txt1);
		t.linkChildFields = splitToArray(txt2);
		if (t.linkChildFields.length != t.linkParentFields.length) {
			Form.logger.error(
					"Child form has specified {} child link fields, but {} prent link fields. Can not link with suh a mismatch",
					t.linkChildFields.length, t.linkParentFields.length);
			t.linkChildFields = null;
			t.linkParentFields = null;
		}
		/*
		 * 10th column is options, but default value is true!!
		 */
		t.isEditable = true;
		Cell cell = row.getCell(9);
		if (cell != null
				&& (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN || cell.getCellType() == Cell.CELL_TYPE_STRING)) {
			t.isEditable = XlsUtil.boolValueOf(cell);
		}
		return t;
	}

	protected static String[] splitToArray(String text) {
		String result[] = text.split(",");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}

	protected static ColumnType parseColumnType(Cell cell) {
		if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
			return null;
		}
		String val = cell.getStringCellValue().trim();
		for (ColumnType ct : ColumnType.values()) {
			if (ct.name().equalsIgnoreCase(val)) {
				return ct;
			}
		}
		logger.error("{} is not a valid column type.", val);
		return null;
	}

	/**
	 * using a builder to accumulate rows for a list and then create a list
	 * 
	 */
	static class ValueListBuilder {
		private Map<String, ValueList> lists = new HashMap<>();
		private String name = null;
		private List<Pair> pairs = new ArrayList<>();

		protected ValueListBuilder() {
			//
		}

		public Map<String, ValueList> done() {
			this.build();
			Map<String, ValueList> result = this.lists;
			this.lists = new HashMap<>();
			return result;
		}

		/**
		 * add row to the builder.
		 * 
		 * @param row
		 */
		void addRow(Row row) {
			String newName = XlsUtil.textValueOf(row.getCell(0));
			Object val = XlsUtil.objectValueOf(row.getCell(1));
			String label = XlsUtil.textValueOf(row.getCell(2));
			if (this.name == null) {
				/*
				 * this is the very first row being read.
				 */
				if (newName == null) {
					AppComps.logger.error("name of the list not mentioned? row {} skipped...", row.getRowNum());
					return;
				}
				this.newList(newName);
			} else if (newName != null && newName.equals(this.name) == false) {
				/*
				 * this row is for the next list. build the previous one.
				 */
				this.build();
				this.newList(newName);
			}

			this.pairs.add(new Pair(label, val));
		}

		private void newList(String newName) {
			this.pairs.clear();
			this.name = newName;
			if (newName != null) {
				AppComps.logger.info("New valueList initiated for {} ", this.name);
			}
		}

		private void build() {
			if (this.name == null) {
				AppComps.logger.error("empty line in lists??, Your list may be all mixed-up!!.");
				return;
			}
			this.lists.put(this.name, new ValueList(this.name, this.pairs.toArray(new Pair[0])));
			AppComps.logger.info("Value list {} pared and added to the map", this.name);
		}
	}

}
