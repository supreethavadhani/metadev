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

/**
 * represents form level validation (across fields) in a form.
 * 
 * @author simplity.org
 *
 */
class ExclusivePair {
	private static final String C = ", ";

	String field1;
	String field2;
	int index1;
	int index2;
	String fieldName;
	boolean isRequired;
	String errorId;

	void emitJavaCode(StringBuilder sbf) {
		sbf.append("new ExclusiveValidation(").append(this.index1);
		sbf.append(C).append(this.index2);
		sbf.append(C).append(this.isRequired);
		sbf.append(C).append(Util.escape(this.fieldName));
		sbf.append(C).append(Util.escape(this.errorId));
		sbf.append(")");
	}
	/**
	 * @param sbf
	 */
	public void emitTs(StringBuilder sbf) {
		sbf.append("{type: 'excl', errorId: '").append(this.errorId).append("', f1: '").append(this.field1);
		sbf.append("', f2: '").append(this.field2).append("', atLeastOne: ").append(this.isRequired).append("}");
	}
}

