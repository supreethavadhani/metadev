package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class BloodGroup extends ValueList {
	 private static final Object[][] VALUES = { 
		{"O -ve", "O -ve"}, 
		{"O +ve", "O +ve"}, 
		{"A -ve", "A -ve"}, 
		{"A +ve", "A +ve"}, 
		{"B -ve", "B -ve"}, 
		{"B +ve", "B +ve"}, 
		{"AB -ve", "AB -ve"}, 
		{"AB +ve", "AB +ve"}
	};
	 private static final String NAME = "bloodGroup";

/**
 *
	 * @param name
	 * @param valueList
 */
	public BloodGroup(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *bloodGroup
 */
	public BloodGroup() {
		super(NAME, VALUES);
	}
}
