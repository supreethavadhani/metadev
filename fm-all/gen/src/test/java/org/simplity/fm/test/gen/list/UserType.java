package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class UserType extends ValueList {
	 private static final Object[][] VALUES = { 
		{"student", null}, 
		{"staff", null}, 
		{"admin", null}, 
		{"guardian", null}, 
		{"trustee", null}
	};
	 private static final String NAME = "userType";

/**
 *
	 * @param name
	 * @param valueList
 */
	public UserType(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *userType
 */
	public UserType() {
		super(NAME, VALUES);
	}
}
