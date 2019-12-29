package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of schema1
 */ 
public class Schema1DataTable extends SchemaDataTable {

	protected Schema1DataTable(final Schema1 schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public Schema1Data getSchemaData(final int idx) {
		return(Schema1Data) super.getSchemaData(idx);
	}
}
