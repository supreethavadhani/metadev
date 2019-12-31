package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class DomicileState extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Karnataka", "Karnataka"}, 
		{"Non-Karnataka", "Non-Karnataka"}, 
		{"Foreign", "Foreign"}
	};
	 private static final String NAME = "domicileState";

/**
 *
	 * @param name
	 * @param valueList
 */
	public DomicileState(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *domicileState
 */
	public DomicileState() {
		super(NAME, VALUES);
	}
}
