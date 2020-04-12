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

import java.util.Map;

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
	int minRows;
	int maxRows;
	String errorId;
	String[] parentLinkFields;
	String[] childLinkFields;

	String label;
	boolean isEditable;
	boolean isTabular;
	int index;

	void emitJavaConstant(final StringBuilder sbf, final int idx) {
		sbf.append("\n\tpublic static final int ").append(this.name).append(" = ").append(idx).append(';');
	}

	/**
	 * push this as an element of an array
	 *
	 * @param sbf
	 */
	void emitJavaCode(final StringBuilder sbf, final Map<String, Field> fields) {
		sbf.append("new LinkedForm(");

		sbf.append(Util.escape(this.name));
		sbf.append(C).append(Util.escape(this.formName));
		sbf.append(C).append(this.minRows);
		sbf.append(C).append(this.maxRows);
		sbf.append(C).append(Util.escape(this.errorId));

		boolean linkExists = false;
		if (this.parentLinkFields == null) {
			if (this.childLinkFields != null) {
				Form.logger.error("childLinkFields ignored as parentLinkFields not specified");
			}
		} else {
			if (this.childLinkFields == null) {
				Form.logger.error("parentLinkFields ignored as childLinkFieldsnot specified");
			} else {
				linkExists = true;
			}
		}
		if (linkExists) {
			sbf.append(C);
			for (final String s : this.parentLinkFields) {
				if (fields.get(s) == null) {
					final String msg = "link field " + s
							+ " is not defined as a field in this form. generating jave code tha will give compilation error";
					Form.logger.error("msg");
					sbf.append(msg);

				}
			}
			Util.emitArray(this.parentLinkFields, sbf);

			sbf.append(C);
			Util.emitArray(this.childLinkFields, sbf);
		} else {
			sbf.append(",null ,null");
		}
		sbf.append(C).append(this.isTabular);
		sbf.append(')');
	}

	String getFormName() {
		return this.formName;
	}

	void emitTs(final StringBuilder sbf) {
		final String T = ",\n\t\t";
		sbf.append("\n\t").append(this.name).append(": ChildForm = {");
		sbf.append("\n\t\tname:").append(Util.escapeTs(this.name));
		sbf.append(T).append("form:").append(Util.toClassName(this.formName)).append("Form.getInstance()");
		sbf.append(T).append("isEditable:").append(this.isEditable);
		sbf.append(T).append("isTabular:").append(this.isTabular);
		sbf.append(T).append("label:").append(this.label == null ? "''" : Util.escapeTs(this.label));
		sbf.append(T).append("minRows:").append(this.minRows);
		sbf.append(T).append("maxRows:").append(this.maxRows);
		sbf.append(T).append("errorId:").append(Util.escapeTs(this.errorId));

		sbf.append("\n\t};");
	}

	/**
	 * @param sbf
	 */
	void emitJavaGetSetter(final StringBuilder sbf) {
		final String c = Util.toClassName(this.formName);
		sbf.append("\n\n\t/** get form table for this linked form ").append(c).append(" **/");
		sbf.append("\n\tpublic ").append(c).append("Fdt get").append(c).append("Fdt() {");
		sbf.append("\n\t\treturn (").append(c).append("Fdt)this.linkedData[").append(this.index).append("];\n\t}");

		sbf.append("\n\n\t/** set form table for this linked form ").append(c).append(" **/");
		sbf.append("\n\tpublic void set").append(c).append("Fdt(").append(c).append("Fdt fdt) {");
		sbf.append("\n\t\t this.linkedData[").append(this.index).append("] = (").append(c).append("Fdt) fdt;\n\t}");

	}
}
