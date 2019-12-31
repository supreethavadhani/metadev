package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of institute
 */ 
public class InstituteDataTable extends SchemaDataTable {

	protected InstituteDataTable(final Institute schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public InstituteData getSchemaData(final int idx) {
		return(InstituteData) super.getSchemaData(idx);
	}
}
