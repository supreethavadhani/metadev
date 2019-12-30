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
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.simplity.fm.core.datatypes.BooleanType;
import org.simplity.fm.core.datatypes.DateType;
import org.simplity.fm.core.datatypes.DecimalType;
import org.simplity.fm.core.datatypes.IntegerType;
import org.simplity.fm.core.datatypes.TextType;
import org.simplity.fm.core.datatypes.TimestampType;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Utility methods for dealing with work book
 *
 * @author simplity.org
 *
 */
class Util {
	static final String[] JAVA_VALUE_TYPES = getJavaValueTypes();
	static final String[] JAVA_GET_TYPES = getJavaGetTypes();

	private static String[] getJavaValueTypes() {
		final ValueType[] types = ValueType.values();

		final String[] result = new String[types.length];
		result[ValueType.Boolean.ordinal()] = "boolean";
		result[ValueType.Date.ordinal()] = "LocalDate";
		result[ValueType.Decimal.ordinal()] = "double";
		result[ValueType.Integer.ordinal()] = "long";
		result[ValueType.Text.ordinal()] = "String";
		result[ValueType.Timestamp.ordinal()] = "Instant";
		return result;
	}

	private static String[] getJavaGetTypes() {
		final ValueType[] types = ValueType.values();

		final String[] result = new String[types.length];
		result[ValueType.Boolean.ordinal()] = "Bool";
		result[ValueType.Date.ordinal()] = "Date";
		result[ValueType.Decimal.ordinal()] = "Decimal";
		result[ValueType.Integer.ordinal()] = "Long";
		result[ValueType.Text.ordinal()] = "String";
		result[ValueType.Timestamp.ordinal()] = "Timestamp";
		return result;
	}

	/**
	 * Gson is not a small object. It is immutable and thread safe. Hence with
	 * this small trick, we can avoid repeated creation of Gson instances
	 */
	public static final Gson GSON = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	/**
	 * this is actually just string escape, nothing to do with XLSX
	 *
	 * @param s
	 * @return string with \ and " escaped for it to be printed inside quotes as
	 *         java literal
	 */
	static String escape(final String s) {
		if (s == null || s.isEmpty()) {
			return "null";
		}
		return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
	}

	/**
	 * type-script prefers single quotes
	 */
	static String escapeTs(final String s) {
		if (s == null) {
			return "null";
		}
		return '\'' + s.replace("\\", "\\\\").replace("'", "''") + '\'';
	}

	/**
	 * type-script prefers single quotes
	 */
	static String escapeTs(final Object obj) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof String) {
			return escapeTs((String) obj);
		}
		return obj.toString();
	}

	/**
	 * write an import statement for the class
	 *
	 * @param sbf
	 * @param cls
	 */
	static void emitImport(final StringBuilder sbf, final Class<?> cls) {
		sbf.append("\nimport ").append(cls.getName()).append(';');
	}

	static String toClassName(final String name) {
		String nam = name;
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			idx++;
			if (idx == nam.length()) {
				return "";
			}
			nam = name.substring(idx);
		}
		return nam.substring(0, 1).toUpperCase() + nam.substring(1);
	}

	static String getClassQualifier(final String name) {
		final int idx = name.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return name.substring(0, idx);
	}

	static void writeOut(final String fileName, final StringBuilder sbf) {
		try (Writer writer = new FileWriter(new File(fileName))) {
			writer.write(sbf.toString());
			logger.info("File {} generated.", fileName);
		} catch (final Exception e) {
			logger.error("Error while writing file {} \n {}", fileName, e.getMessage());
		}

	}

	/**
	 *
	 * @param obj
	 * @return value of this object, quoted if required
	 */
	static Object escapeObject(final Object obj) {
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
	static Class<?> getDataTypeClass(final ValueType valueType) {
		switch (valueType) {
		case Boolean:
			return BooleanType.class;
		case Date:
			return DateType.class;
		case Decimal:
			return DecimalType.class;
		case Integer:
			return IntegerType.class;
		case Text:
			return TextType.class;
		case Timestamp:
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
	static int getValueTypeIdx(final ValueType valueType) {
		switch (valueType) {
		case Text:
			return 0;
		case Integer:
			return 1;
		case Decimal:
			return 2;
		case Boolean:
			return 3;
		case Date:
			return 4;
		case Timestamp:
			return 5;
		default:
			return -1;
		}
	}

	/**
	 * parse the Json member as a Map. Add the key/index as a field for the
	 * parsed object
	 *
	 * @param map
	 *            non-null map to which this collection is to be parsed into
	 * @param parentJson
	 *            json that is to optionally contain JsonObject as member with
	 *            this name
	 * @param memberName
	 *            name with which to find the json for the ,ap to be parsed
	 * @param field
	 *            to which the key/index is to be set to. null if this is not
	 *            required. use getField() to get this
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void addToMap(final Map map, final JsonReader reader, final Class cls) throws IOException {
		logger.info("Started parsing map of class {}", cls.getName());
		final int nbr = map.size();
		int idx = nbr - 1;
		reader.beginObject();
		while (true) {
			idx++;
			final JsonToken token = reader.peek();
			if (token == JsonToken.END_OBJECT) {
				logger.info("{} objects parsed", (nbr - idx));
				reader.endObject();
				return;
			}

			final String key = reader.nextName();
			final Object value = GSON.fromJson(reader, cls);
			if (value instanceof INamedMember) {
				((INamedMember) value).setNameAndIdx(key, idx);
			}
			map.put(key, value);
		}
	}

	/**
	 * parse and return a map
	 *
	 * @param reader
	 * @param cls
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static void loadMap(final Map map, final JsonReader reader, final Class cls) throws IOException {
		logger.info("Started loading instances of {} into a map", cls.getName());
		final int nbr = map.size();
		int idx = nbr - 1;
		reader.beginObject();
		while (true) {
			idx++;
			final JsonToken token = reader.peek();
			if (token == JsonToken.END_OBJECT) {
				reader.endObject();
				logger.info("{} objects parsed", (idx - nbr));
				return;
			}

			final String key = reader.nextName();
			ISelfLoader value;
			try {
				value = (ISelfLoader) cls.newInstance();
				value.fromJson(reader, key, idx);
				map.put(key, value);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * object populates itself from a name-object json member.
	 */
	interface INamedMember {
		/**
		 *
		 * @param name
		 *            member name. typically becomes name attribute of the
		 *            object
		 * @param idx
		 *            0-based index of this member. this is generally not the
		 *            right thing to do in a json because the order of
		 *            attributes is not significant.However,in our design, we
		 *            would like to make use of it and hence this
		 */
		void setNameAndIdx(String name, int idx);
	}

	/**
	 * object populates itself from a Json name-member pair.
	 */
	interface ISelfLoader {
		/**
		 *
		 * @param reader
		 *            is positioned at begin_object (it is not consumed. only
		 *            name is consumed) caller is expected to consume the object
		 *            and return
		 * @param key
		 *            name that is already parsed
		 * @param idx
		 *            0-based index of this member. this is generally not the
		 *            right thing to do in a json because the order of
		 *            attributes is not significant.However,in our design, we
		 *            would like to make use of it and hence this
		 * @throws IOException
		 */
		void fromJson(JsonReader reader, String key, int idx) throws IOException;
	}

	/**
	 * @param reader
	 * @throws IOException
	 */
	public static void swallowAToken(final JsonReader reader) throws IOException {
		final JsonToken token = reader.peek();
		switch (token) {

		case BEGIN_ARRAY:
			GSON.fromJson(reader, Object[].class);
			return;

		case BEGIN_OBJECT:
			GSON.fromJson(reader, Object.class);
			return;

		case BOOLEAN:
		case NUMBER:
		case STRING:
			reader.nextString();
			return;

		case NAME:
			reader.nextName();
			return;

		case NULL:
			reader.nextNull();
			return;

		case END_ARRAY:
			reader.endArray();
			return;
		case END_OBJECT:
			reader.endArray();
			return;
		case END_DOCUMENT:
			return;
		default:
			logger.warn("Util is not designed to swallow the token {} ", token.name());
		}
	}

	public static void emitArray(final String[] arr, final StringBuilder sbf) {
		sbf.append("new String[]{");
		boolean firstOne = true;
		for (final String s : arr) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(',');
			}
			sbf.append(escape(s));
		}
		sbf.append('}');
	}
}
