package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class LevelList extends RuntimeList {
	 private static final String NAME = "levelList";
	 private static final String LIST_SQL = "SELECT level_id, name FROM levels WHERE program_id=?";
	 private static final String CHECK_SQL = "SELECT level_id FROM levels WHERE level_id=? and program_id=?";
	/**
	 *
	 */
	public LevelList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.hasKey = true;
	}
}
