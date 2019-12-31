package org.simplity.fm.test.gen.list;

import org.simplity.fm.core.validn.ValueList;
public class DocumentType extends ValueList {
	 private static final Object[][] VALUES = { 
		{"Marks Card", "Marks Card"}, 
		{"Certificate", "Ceritifcate (e.g: Birth Certificate etc)"}, 
		{"Photo", "Photo"}, 
		{"Govt Id", "Govt Id(e.g.: Passport, DL etc)"}
	};
	 private static final String NAME = "documentType";

/**
 *
	 * @param name
	 * @param valueList
 */
	public DocumentType(String name, Object[][] valueList) {
		super(name, valueList);
	}

/**
 *documentType
 */
	public DocumentType() {
		super(NAME, VALUES);
	}
}
