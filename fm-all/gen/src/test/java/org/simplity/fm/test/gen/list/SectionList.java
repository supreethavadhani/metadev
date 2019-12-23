package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class SectionList extends RuntimeList {
	 private static final String NAME = "sectionList";
	 private static final String LIST_SQL = "SELECT section_id, name FROM sections WHERE level_id=?";
	 private static final String CHECK_SQL = "SELECT section_id FROM sections WHERE section_id=? and level_id=?";
	/**
	 *
	 */
	public SectionList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.hasKey = true;
	}
}
