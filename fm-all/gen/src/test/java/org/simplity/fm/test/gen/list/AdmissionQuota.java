package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class AdmissionQuota extends ValueList {
	 private static final Object[][] VALUES = { 
		{"CET", null}, 
		{"COMEDK", null}, 
		{"CETSNQ", null}, 
		{"MANG", null}, 
		{"NRI", null}, 
		{"GOI", null}, 
		{"Other", null}
	};
	 private static final String NAME = "admissionQuota";

/**
 *
	 * @param name
	 * @param valueList
 */
	public AdmissionQuota(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *admissionQuota
 */
	public AdmissionQuota() {
		super(NAME, VALUES);
	}
}
