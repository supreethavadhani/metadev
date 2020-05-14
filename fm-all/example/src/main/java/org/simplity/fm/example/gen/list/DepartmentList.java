package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class DepartmentList extends RuntimeList {
	 private static final String NAME = "departmentList";
	 private static final String LIST_SQL = "SELECT department_id, name FROM departments";
	 private static final String CHECK_SQL = "SELECT department_id FROM departments WHERE department_id=?";
	/**
	 *
	 */
	public DepartmentList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
