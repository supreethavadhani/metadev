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
 * @author simplity.org
 *
 */
class RuntimeList {
	private static final String C = ", ";
	String name;
	String table;
	String col1;
	String col2;
	String key;
	boolean keyIsNumeric;
	boolean valueIsNumeric;
	String tenantColumnName;

	void emitJava(StringBuilder sbf, String packageName) {
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, org.simplity.fm.core.validn.RuntimeList.class);

		sbf.append("\npublic class ").append(Util.toClassName(this.name)).append(" extends RuntimeList {");

		sbf.append("\n\t private static final String NAME = \"").append(this.name).append("\";");
		
		sbf.append("\n\t private static final String LIST_SQL = \"SELECT ");
		sbf.append(this.col1).append(C).append(this.col2).append(" FROM ").append(this.table);

		if(this.key != null) {
			sbf.append(" WHERE ").append(this.key).append("=?");
		}
		if(this.tenantColumnName != null) {
			if(this.key == null) {
				sbf.append(" WHERE ");
			}else {
				sbf.append(" AND ");
			}
			sbf.append(this.tenantColumnName).append("=?");
		}
		sbf.append("\";");
		
		sbf.append("\n\t private static final String CHECK_SQL = \"SELECT ").append(this.col1).append(" FROM ").append(this.table);
		sbf.append(" WHERE ").append(this.col1).append("=?");
		if(this.key != null) {
			sbf.append(" and ").append(this.key).append("=?");
		}
		sbf.append("\";");
		
		sbf.append("\n\t private static final boolean HAS_KEY = ");
		if(this.key == null) {
			sbf.append("false;");
		}else {
			sbf.append("true;");
			sbf.append("\n\t private static final boolean KEY_IS_NUMERIC = ").append(this.keyIsNumeric).append(";");
		}
		sbf.append("\n\t private static final boolean VALUE_IS_NUMERIC = ").append(this.valueIsNumeric).append(";");
		
		sbf.append("\n\t/**\n\t *\n\t */\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tthis.listSql = LIST_SQL;");
		sbf.append("\n\t\tthis.checkSql = CHECK_SQL;");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.hasKey = HAS_KEY;");
		sbf.append("\n\t\tthis.valueIsNumeric = VALUE_IS_NUMERIC;");
		if(this.key != null) {
			sbf.append("\n\t\tthis.keyIsNumeric = KEY_IS_NUMERIC;");
		}
		sbf.append("\n\t}\n}\n");
	}
}