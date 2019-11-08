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

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * represents a row in our spreadsheet for each data type
 *
 * @author simplity.org
 *
 */
class DataTypes {
	protected static final Logger logger = LoggerFactory.getLogger(DataTypes.class);
	private static final String C = ", ";
	private static final String NAME = "name";

	Map<String, DataType> dataTypes;

	public void fromJson(final JsonObject json) {
		this.dataTypes = new HashMap<>();
		this.accumulate(json, TextType.class);
		this.accumulate(json, IntegerType.class);
		this.accumulate(json, DecimalType.class);
		this.accumulate(json, BooleanType.class);
		this.accumulate(json, DateType.class);
		this.accumulate(json, TimestampType.class);
	}

	private void accumulate(final JsonObject json, final Class<? extends DataType> cls) {
		String nam = cls.getSimpleName();
		nam = nam.substring(0, 1).toLowerCase() + nam.substring(1) + 's';
		final Map<String, ? extends DataType> map = JsonUtil.fromJson(json, nam, cls, NAME);
		final int n = map.size();
		if (n == 0) {
			logger.info("No {} defiined", nam);
			return;
		}

		logger.info("{} {} extracted", map.size(), nam);
		this.dataTypes.putAll(map);

	}

	protected abstract static class DataType {
		String name;
		String errorId;
	}

	protected static class BooleanType extends DataType {
		String trueLabel;
		String falseLabel;
	}

	protected static class DateType extends DataType {
		String maxPastDays;
		String maxFutureDays;
	}

	protected static class IntegerType extends DataType {
		long minValue;
		long maxValue;
	}

	protected static class DecimalType extends DataType {
		long minValue;
		long maxVakue;
		int nbrFractions;
	}

	protected static class TimestampType extends DataType {
		String maxPastDays;
		String maxFutureDays;
	}

	protected static class TextType extends DataType {
		String regex;
		int minLength;
		int maxLength;
	}

	/*
	 * all columns in the fields sheet
	 */
	String name;
	ValueType valueType;
	String errorId;
	String regexDesc;
	String regex;
	int minLength;
	int maxLength;
	long minValue;
	long maxValue;
	String trueLabel;
	String falseLabel;
	int nbrFractions;

	void emitJava(final StringBuilder sbf) {
		final String cls = Util.getDataTypeClass(this.valueType).getSimpleName();
		/*
		 * following is the type of line to be output
		 * public static final {className} {fieldName} = new
		 * {className}({errorMessageId}.......);
		 */
		sbf.append("\n\tpublic static final ").append(cls).append(" ").append(this.name);
		sbf.append(" = new ").append(cls).append("(");
		sbf.append(Util.escape(this.name));
		sbf.append(C).append(Util.escape(this.errorId));
		/*
		 * append parameters list based on the data type
		 */
		this.appendDtParams(sbf);
		/*
		 * close the constructor and we are done
		 */
		sbf.append(");");
	}

	private void appendDtParams(final StringBuilder sbf) {
		switch (this.valueType) {
		case TIMESTAMP:
		case BOOLEAN:
			return;

		case DATE:
		case INTEGER:
			sbf.append(C).append(this.minValue).append("L, ").append(this.maxValue).append('L');
			return;

		case DECIMAL:
			sbf.append(C).append(this.minValue).append("L, ").append(this.maxValue).append('L').append(C)
					.append(this.nbrFractions);
			return;
		case TEXT:
			sbf.append(C).append(this.minLength).append(C).append(this.maxLength).append(C)
					.append(Util.escape(this.regex));
			return;
		default:
			sbf.append(" generating compilation error on valueType=" + this.valueType);
			return;
		}
	}

}
