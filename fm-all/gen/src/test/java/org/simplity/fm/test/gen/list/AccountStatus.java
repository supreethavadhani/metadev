package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class AccountStatus extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Active", "Active"}, 
		{"Suspended", "Suspended"}, 
		{"Closed", "Closed"}
	};
	 private static final String NAME = "accountStatus";

/**
 *
	 * @param name
	 * @param valueList
 */
	public AccountStatus(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *accountStatus
 */
	public AccountStatus() {
		super(NAME, VALUES);
	}
}
