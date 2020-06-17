package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class Gender extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Male", null}, 
		{"Female", null}, 
		{"Others", null}, 
		{"Not Applicable", null}
	};
	 private static final String NAME = "gender";

/**
 *
	 * @param name
	 * @param valueList
 */
	public Gender(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *gender
 */
	public Gender() {
		super(NAME, VALUES);
	}
}
