package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class SubjectSectionList extends RuntimeList {
	 private static final String NAME = "subjectSectionList";
	 private static final String LIST_SQL = "SELECT subject_section_id, name FROM subject_section_list WHERE offered_subject_id=?";
	 private static final String CHECK_SQL = "SELECT subject_section_id FROM subject_section_list WHERE subject_section_id=? AND offered_subject_id=?";
	/**
	 *
	 */
	public SubjectSectionList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
