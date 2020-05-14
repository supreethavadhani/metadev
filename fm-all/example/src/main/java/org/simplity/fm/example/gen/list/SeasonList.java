package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class SeasonList extends RuntimeList {
	 private static final String NAME = "seasonList";
	 private static final String LIST_SQL = "SELECT season_id, name FROM seasons";
	 private static final String CHECK_SQL = "SELECT season_id FROM seasons WHERE season_id=?";
	/**
	 *
	 */
	public SeasonList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
