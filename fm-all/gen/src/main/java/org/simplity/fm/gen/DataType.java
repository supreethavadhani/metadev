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

import org.simplity.fm.core.datatypes.ValueType;

/**
 * represents a row in our spreadsheet for each data type
 * 
 * @author simplity.org
 *
 */
class DataType {
	private static final String C = ", ";
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

	void emitJava(StringBuilder sbf) {
		String cls = Util.getDataTypeClass(this.valueType).getSimpleName();
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

	private void appendDtParams(StringBuilder sbf) {
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
