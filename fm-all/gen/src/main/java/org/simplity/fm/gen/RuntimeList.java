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

import org.simplity.fm.gen.Util.INamedMember;

/**
 * @author simplity.org
 *
 */
class RuntimeList implements INamedMember {
	private static final String C = ", ";
	private static final String WHERE = " WHERE ";
	private static final String AND = " AND ";

	String name;
	String dbTableName;
	String dbColumn1;
	String dbColumn2;
	String keyColumn;
	boolean keyIsNumeric;
	boolean valueIsNumeric;
	String tenantColumnName;
	String activeColumnName;
	/*
	 * in case this list is also required in batches
	 */
	String parentTable;
	String parentIdColumnName;
	String parentNameColumnName;

	@Override
	public void setNameAndIdx(final String name, final int idx) {
		this.name = name;
	}

	void emitJava(final StringBuilder sbf, final String packageName) {
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, org.simplity.fm.core.validn.RuntimeList.class);

		sbf.append("\npublic class ").append(Util.toClassName(this.name)).append(" extends RuntimeList {");

		sbf.append("\n\t private static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\t private static final String LIST_SQL = \"SELECT ");
		sbf.append(this.dbColumn1).append(C).append(this.dbColumn2).append(" FROM ").append(this.dbTableName);

		boolean whereAdded = false;
		if (this.activeColumnName != null) {
			sbf.append(WHERE).append(this.activeColumnName).append("=true");
			whereAdded = true;
		}
		if (this.keyColumn != null) {
			if (whereAdded) {
				sbf.append(AND);
			} else {
				sbf.append(WHERE);
				whereAdded = true;
			}
			sbf.append(this.keyColumn).append("=?");
		}
		if (this.tenantColumnName != null) {
			if (whereAdded) {
				sbf.append(AND);
			} else {
				sbf.append(WHERE);
				whereAdded = true;
			}
			sbf.append(this.tenantColumnName).append("=?");
		}
		sbf.append("\";");

		sbf.append("\n\t private static final String CHECK_SQL = \"SELECT ").append(this.dbColumn1).append(" FROM ")
				.append(this.dbTableName);
		sbf.append(WHERE).append(this.dbColumn1).append("=?");
		if (this.activeColumnName != null) {
			sbf.append(AND).append(this.activeColumnName).append("=true");
		}
		if (this.keyColumn != null) {
			sbf.append(AND).append(this.keyColumn).append("=?");
		}
		sbf.append("\";");

		if (this.parentTable != null) {
			sbf.append("\n\t private static final String ALL_SQL = \"SELECT a.").append(this.dbColumn1);
			sbf.append(", a.").append(this.dbColumn2).append(", b.").append(this.parentNameColumnName).append(" FROM ");
			sbf.append(this.dbTableName).append(" a, ").append(this.parentTable).append(" b ");
			sbf.append(WHERE).append("a.").append(this.keyColumn).append("=b.").append(this.parentIdColumnName);
			if (this.tenantColumnName != null) {
				sbf.append(AND).append(this.tenantColumnName).append("=?");
			}
			sbf.append("\";");
		}

		sbf.append("\n\t/**\n\t *\n\t */\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.listSql = LIST_SQL;");
		sbf.append("\n\t\tthis.checkSql = CHECK_SQL;");

		if (this.valueIsNumeric) {
			sbf.append("\n\t\tthis.valueIsNumeric = true;");
		}

		if (this.keyColumn != null) {
			sbf.append("\n\t\tthis.hasKey = true;");
			if (this.keyIsNumeric) {
				sbf.append("\n\t\tthis.keyIsNumeric = true;");
			}
		}

		if (this.tenantColumnName != null) {
			sbf.append("\n\t\tthis.isTenantSpecific = true;");
		}

		sbf.append("\n\t}\n}\n");
	}
}