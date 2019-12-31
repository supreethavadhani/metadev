package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class Religion extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Hindu", "Hindu"}, 
		{"Muslim", "Muslim"}, 
		{"Christian", "Christian"}, 
		{"Sikh", "Sikh"}, 
		{"Jain", "Jain"}, 
		{"Others", "Other"}
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
