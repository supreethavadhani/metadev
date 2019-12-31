package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of trustee
 */ 
public class TrusteeDataTable extends SchemaDataTable {

	protected TrusteeDataTable(final Trustee schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public TrusteeData getSchemaData(final int idx) {
		return(TrusteeData) super.getSchemaData(idx);
	}
}
