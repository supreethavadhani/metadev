package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class OfferedSubjectList extends RuntimeList {
	 private static final String NAME = "offeredSubjectList";
	 private static final String LIST_SQL = "SELECT offered_subject_id, name FROM offered_subject_list WHERE department_id=?";
	 private static final String CHECK_SQL = "SELECT offered_subject_id FROM offered_subject_list WHERE offered_subject_id=? AND department_id=?";
	/**
	 *
	 */
	public OfferedSubjectList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
