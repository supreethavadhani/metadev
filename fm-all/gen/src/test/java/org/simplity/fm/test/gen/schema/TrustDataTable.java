package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of trust
 */ 
public class TrustDataTable extends SchemaDataTable {

	protected TrustDataTable(final Trust schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public TrustData getSchemaData(final int idx) {
		return(TrustData) super.getSchemaData(idx);
	}
}
