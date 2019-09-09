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
import java.io.FileWriter;
import java.io.Writer;


import org.simplity.fm.core.datatypes.BooleanType;
import org.simplity.fm.core.datatypes.DateType;
import org.simplity.fm.core.datatypes.DecimalType;
import org.simplity.fm.core.datatypes.IntegerType;
import org.simplity.fm.core.datatypes.TextType;
import org.simplity.fm.core.datatypes.TimestampType;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with work book
 * 
 * @author simplity.org
 *
 */
class Util {
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	/**
	 * this is actually just string escape, nothing to do with XLSX
	 * 
	 * @param s
	 * @return string with \ and " escaped for it to be printed inside quotes as
	 *         java literal
	 */
	static String escape(String s) {
		if (s == null || s.isEmpty()) {
			return "null";
		}
		return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
	}

	/**
	 * type-script prefers single quotes
	 */
	static String escapeTs(String s) {
		if (s == null) {
			return "null";
		}
		return '\'' + s.replace("\\", "\\\\").replace("'", "''") + '\'';
	}

	/**
	 * type-script prefers single quotes
	 */
	static String escapeTs(Object obj) {
		if (obj == null) {
			return "null";
		}
		if(obj instanceof String) {
			return escapeTs((String)obj);
		}
		return obj.toString();
	}

	/**
	 * write an import statement for the class
	 * 
	 * @param sbf
	 * @param cls
	 */
	static void emitImport(StringBuilder sbf, Class<?> cls) {
		sbf.append("\nimport ").append(cls.getName()).append(';');
	}

	static String toClassName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	static void writeOut(String fileName, StringBuilder sbf) {
		try (Writer writer = new FileWriter(new File(fileName))) {
			writer.write(sbf.toString());
			logger.info("File {} generated.", fileName);
		} catch (Exception e) {
			logger.error("Error while writing file {} \n {}", fileName, e.getMessage());
		}

	}

	/**
	 * 
	 * @param obj
	 * @return value of this object, quoted if required
	 */
	static Object escapeObject(Object obj) {
		if (obj == null) {
			return "null";
		}

		if (obj instanceof String) {
			return escape((String) obj);
		}

		return obj.toString();
	}

	/**
	 * 
	 * @param valueType
	 * @return data type class name for this value type
	 */
	static Class<?> getDataTypeClass(ValueType valueType) {
		switch (valueType) {
		case BOOLEAN:
			return BooleanType.class;
		case DATE:
			return DateType.class;
		case DECIMAL:
			return DecimalType.class;
		case INTEGER:
			return IntegerType.class;
		case TEXT:
			return TextType.class;
		case TIMESTAMP:
			return TimestampType.class;
		default:
			logger.error("{} is not a known value type", valueType);
			return TextType.class;
		}
	}

	/**
	 * 
	 * @param valueType
	 * @return get index used by the client for this value type
	 */
	static int getValueTypeIdx(ValueType valueType) {
		switch (valueType) {
		case TEXT:
			return 0;
		case INTEGER:
			return 1;
		case DECIMAL:
			return 2;
		case BOOLEAN:
			return 3;
		case DATE:
			return 4;
		case TIMESTAMP:
			return 5;
		default:
			return -1;
		}
	}

}
