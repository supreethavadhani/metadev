package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class InstituteType extends ValueList {
	 private static final Object[][] VALUES = { 
		{"DSERTPS", "Karnataka State Syllabus Primary School"}, 
		{"DSERTPHS", "Karnataka State Syllabus Higher School"}, 
		{"CBSE ", "CBSE"}, 
		{"ENG_A_VTU", "Engineering College (Automonus Under VTU)"}, 
		{"ENG_VTU", "Engineering College ( VTU)"}
	};
	 private static final String NAME = "instituteType";

/**
 *
	 * @param name
	 * @param valueList
 */
	public InstituteType(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *instituteType
 */
	public InstituteType() {
		super(NAME, VALUES);
	}
}
