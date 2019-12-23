package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class Religion extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Hindu", null}, 
		{"Muslim", null}, 
		{"Christian", null}, 
		{"Sikh", null}, 
		{"Jain", null}, 
		{"Others", null}
	};
	 private static final String NAME = "religion";

/**
 *
	 * @param name
	 * @param valueList
 */
	public Religion(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *religion
 */
	public Religion() {
		super(NAME, VALUES);
	}
}
