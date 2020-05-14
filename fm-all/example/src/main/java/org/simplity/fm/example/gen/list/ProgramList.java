package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class ProgramList extends RuntimeList {
	 private static final String NAME = "programList";
	 private static final String LIST_SQL = "SELECT program_id, name FROM programs WHERE department_id=?";
	 private static final String CHECK_SQL = "SELECT program_id FROM programs WHERE program_id=? AND department_id=?";
	/**
	 *
	 */
	public ProgramList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
