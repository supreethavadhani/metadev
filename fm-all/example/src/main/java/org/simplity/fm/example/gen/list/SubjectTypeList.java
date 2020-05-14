package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class SubjectTypeList extends RuntimeList {
	 private static final String NAME = "subjectTypeList";
	 private static final String LIST_SQL = "SELECT subject_type_id, name FROM subject_types";
	 private static final String CHECK_SQL = "SELECT subject_type_id FROM subject_types WHERE subject_type_id=?";
	/**
	 *
	 */
	public SubjectTypeList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
