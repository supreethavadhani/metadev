package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class AdmissionQuota extends ValueList {
	 private static final Object[][] VALUES = { 
		{"CET", "CET"}, 
		{"COMEDK", "COMEDK"}, 
		{"CETSNQ", "CET-SNQ"}, 
		{"MANG", "MANAGEMENT"}, 
		{"NRI", "NRI"}, 
		{"GOI", "GOI"}, 
		{"Other", "Other"}
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
