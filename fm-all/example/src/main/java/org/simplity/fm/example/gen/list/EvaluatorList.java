package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;
public class EvaluatorList extends RuntimeList {
	 private static final String NAME = "evaluatorList";
	 private static final String LIST_SQL = "SELECT evaluator_id, name FROM evaluators WHERE offered_subject_id=?";
	 private static final String CHECK_SQL = "SELECT evaluator_id FROM evaluators WHERE evaluator_id=? AND offered_subject_id=?";
	/**
	 *
	 */
	public EvaluatorList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
