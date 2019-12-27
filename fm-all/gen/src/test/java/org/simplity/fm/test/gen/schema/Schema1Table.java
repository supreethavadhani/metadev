package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DataTable;

/**
 * class that represents structure of schema1
 */
public class Schema1Table extends DataTable {

	protected Schema1Table(final Schema1 schema) {
		super(schema);
	}

	protected Schema1Table(final Schema1 schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public Schema1Row getRow(final int idx) {
		return (Schema1Row) super.getRow(idx);
	}
}
