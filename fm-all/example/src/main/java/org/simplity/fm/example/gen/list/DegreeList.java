package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class DegreeList extends RuntimeList {
	 private static final String NAME = "degreeList";
	 private static final String LIST_SQL = "SELECT degree_id, name FROM degrees";
	 private static final String CHECK_SQL = "SELECT degree_id FROM degrees WHERE degree_id=?";
	/**
	 *
	 */
	public DegreeList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
