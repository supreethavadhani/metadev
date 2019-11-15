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
 * represents a Table row in tables sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class LinkedForm {
	private static final String C = ", ";

	String name;
	String formName;
	boolean isArray;
	int minRows;
	int maxRows;
	String errorId;
	String[] parentLinkFields;
	String[] childLinkFields;

	String label;
	boolean isEditable;
	int index;

	void emitJavaConstant(final StringBuilder sbf, final int idx) {
		sbf.append("\n\tpublic static final int ").append(this.name).append(" = ").append(idx).append(';');
	}

	/**
	 * push this as an element of an array
	 *
	 * @param sbf
	 */
	void emitJavaCode(final StringBuilder sbf) {
		sbf.append("new LinkedForm(");

		sbf.append(Util.escape(this.name));
		sbf.append(C).append(Util.escape(this.formName));
		sbf.append(C).append(this.isArray);
		sbf.append(C).append(this.minRows);
		sbf.append(C).append(this.maxRows);
		sbf.append(C).append(Util.escape(this.errorId));

		sbf.append(C);
		Util.emitArray(this.parentLinkFields, sbf);

		sbf.append(C);
		Util.emitArray(this.childLinkFields, sbf);

		sbf.append(')');
	}

	String getFormName() {
		return this.formName;
	}

	void emitTs(final StringBuilder sbf) {
		sbf.append("\n\t").append(this.name).append(": ChildForm = {");
		sbf.append("name:").append(Util.escapeTs(this.name));
		sbf.append("\n\t\t,form:").append(Util.toClassName(this.formName)).append(".getInstance()");
		sbf.append("\n\t\t,isTabular:").append(this.isArray);
		sbf.append("\n\t\t,isEditable:").append(this.isEditable);
		sbf.append("\n\t\t,label:").append(this.label == null ? "''" : Util.escapeTs(this.label));
		sbf.append("\n\t\t,minRows:").append(this.minRows);
		sbf.append("\n\t\t,maxRows:").append(this.maxRows);
		sbf.append("\n\t\t,errorId:").append(Util.escapeTs(this.errorId));

		sbf.append("\n\t};");
	}
}
