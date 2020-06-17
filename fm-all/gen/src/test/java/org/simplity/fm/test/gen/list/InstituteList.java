package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class InstituteList extends RuntimeList {
	 private static final String NAME = "instituteList";
	 private static final String LIST_SQL = "SELECT institute_id, name FROM institutes WHERE trust_id=?";
	 private static final String CHECK_SQL = "SELECT institute_id FROM institutes WHERE institute_id=? and trust_id=?";
	/**
	 *
	 */
	public InstituteList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.hasKey = true;
	}
}
