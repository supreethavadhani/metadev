package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class RoleList extends RuntimeList {
	 private static final String NAME = "roleList";
	 private static final String LIST_SQL = "SELECT role_id, role_name FROM roles";
	 private static final String CHECK_SQL = "SELECT role_id FROM roles WHERE role_id=?";
	/**
	 *
	 */
	public RoleList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
