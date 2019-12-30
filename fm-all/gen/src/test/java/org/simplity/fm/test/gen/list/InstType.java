package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class InstType extends ValueList {
	 private static final Object[][] VALUES = { 
		{"DSERTPS", null}, 
		{"DSERTPHS", null}, 
		{"CBSE ", null}, 
		{"ENG_A_VTU", null}, 
		{"ENG_VTU", null}
	};
	 private static final String NAME = "instType";

/**
 *
	 * @param name
	 * @param valueList
 */
	public InstType(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *instType
 */
	public InstType() {
		super(NAME, VALUES);
	}
}
