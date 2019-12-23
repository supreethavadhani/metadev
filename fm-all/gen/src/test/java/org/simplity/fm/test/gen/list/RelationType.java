package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class RelationType extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Mother", null}, 
		{"Father", null}, 
		{"Legal Guardian", null}
	};
	 private static final String NAME = "relationType";

/**
 *
	 * @param name
	 * @param valueList
 */
	public RelationType(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *relationType
 */
	public RelationType() {
		super(NAME, VALUES);
	}
}
