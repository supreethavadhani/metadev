package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class ValuationRoundList extends ValueList {
	 private static final Object[][] VALUES = { 
		{"1", "Initial"}, 
		{"2", "Revaluation"}, 
		{"3", "Challenge"}
	};
	 private static final String NAME = "valuationRoundList";

/**
 *
	 * @param name
	 * @param valueList
 */
	public ValuationRoundList(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *valuationRoundList
 */
	public ValuationRoundList() {
		super(NAME, VALUES);
	}
}
