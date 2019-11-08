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

package org.simplity.fm.core;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * @author simplity.org
 *
 */
public class JsonUtil {
	private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	private static final char OPEN_ARR = '[';
	private static final char CLOS_ARR = ']';
	private static final char COMA = ',';
	private static final char Q = '"';
	private static final String QS = "\"";
	private static final String QQS = "\"\"";
	private static final String NULL = "null";

	/**
	 * write a 2d array of string
	 *
	 * @param writer
	 * @param arr
	 * @throws IOException
	 */
	public static void write(final Writer writer, final Object[][] arr) throws IOException {
		if (arr == null) {
			writer.write("[[]]");
			return;
		}
		writer.write(OPEN_ARR);
		boolean first = true;
		for (final Object[] row : arr) {
			if (first) {
				first = false;
			} else {
				writer.write(COMA);
			}
			write(writer, row);
		}
		writer.write(CLOS_ARR);
	}

	/**
	 * write an array of string
	 *
	 * @param writer
	 * @param arr
	 * @throws IOException
	 */
	public static void write(final Writer writer, final Object[] arr) throws IOException {
		if (arr == null) {
			writer.write("[]");
			return;
		}
		writer.write(OPEN_ARR);
		boolean first = true;
		for (final Object s : arr) {
			if (first) {
				first = false;
			} else {
				writer.write(COMA);
			}
			write(writer, s);
		}
		writer.write(CLOS_ARR);
	}

	/**
	 * write a string
	 *
	 * @param writer
	 * @param primitive
	 *            value
	 * @throws IOException
	 */
	public static void write(final Writer writer, final Object primitive) throws IOException {
		if (primitive == null) {
			writer.write(NULL);
			return;
		}
		final String s = primitive.toString();
		if (primitive instanceof Number || primitive instanceof Boolean) {
			writer.write(primitive.toString());
			return;
		}
		writer.write(Q);
		writer.write(s.replaceAll(QS, QQS));
		writer.write(Q);
	}

	/**
	 * write a string
	 *
	 * @param sbf
	 * @param number
	 */
	public static void toJson(final StringBuilder sbf, final Number number) {
		if (number == null) {
			sbf.append(NULL);
			return;
		}
		sbf.append("" + number);
	}

	/**
	 * parse a map. optionally copy the key/index to an attribute of the parsed
	 * object member
	 *
	 * @param <T>
	 *            type of the element of the map
	 * @param parentJson
	 *            element tree from which has a member with this map
	 * @param memberName
	 *            name of the attribute in JSON to be
	 * @param cls
	 *            class of the member
	 * @param attName
	 *            attribute name of the member to which the key/index value is
	 *            to be set to. null if this is not required
	 * @return map. empty in case of any issue, but not not null;
	 */
	public static <T> Map<String, T> fromJson(final JsonObject parentJson, final String memberName, final Class<T> cls,
			final String attName) {
		final JsonObject json = parentJson.getAsJsonObject(memberName);
		if (json == null) {
			return new HashMap<>();
		}

		final Type type = new TypeToken<Map<String, T>>() {
			/* */}.getType();
		final Map<String, T> map = new Gson().fromJson(json, type);
		logger.info("{} entries parsed for a map ", map.size());
		if (attName == null) {
			return map;
		}
		try {
			final java.lang.reflect.Field field = cls.getField(attName);
			field.setAccessible(true);
			for (final Map.Entry<String, T> entry : map.entrySet()) {
				field.set(entry.getValue(), entry.getKey());
			}
			logger.info("indexed key is set as {} attribute to al objects in the map", attName);
			return map;
		} catch (final Exception e) {
			logger.error("name is not a field, or it is not accessible");
			return map;
		}
	}

	/**
	 * get the member as string.
	 *
	 * @param json
	 * @param attName
	 * @return null if the member does not exist or is not a primitive. Else
	 *         string value of the primitive
	 */
	public static String getStringMember(final JsonObject json, final String attName) {
		final JsonPrimitive p = json.getAsJsonPrimitive(attName);
		if (p == null) {
			return null;
		}
		return p.getAsString();
	}

	/**
	 * get the member as number.
	 *
	 * @param json
	 * @param attName
	 * @return 0 if the member does not exist or is not a number.
	 */
	public static double getNumberMember(final JsonObject json, final String attName) {
		try {
			return json.getAsJsonPrimitive(attName).getAsDouble();
		} catch (final Exception e) {
			return 0;
		}
	}

	/**
	 * get the member as boolean.
	 *
	 * @param json
	 * @param attName
	 * @return true if the member exists as boolean and its value is true. false
	 *         otherwise
	 */
	public static boolean getBoolMember(final JsonObject json, final String attName) {
		try {
			return json.getAsJsonPrimitive(attName).getAsBoolean();
		} catch (final Exception e) {
			return false;
		}
	}
}
