package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of section
 */ 
public class SectionDataTable extends SchemaDataTable {

	protected SectionDataTable(final Section schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public SectionData getSchemaData(final int idx) {
		return(SectionData) super.getSchemaData(idx);
	}
}
