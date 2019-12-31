package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of guardian
 */ 
public class GuardianDataTable extends SchemaDataTable {

	protected GuardianDataTable(final Guardian schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public GuardianData getSchemaData(final int idx) {
		return(GuardianData) super.getSchemaData(idx);
	}
}
