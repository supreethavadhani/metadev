package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of department
 */ 
public class DepartmentDataTable extends SchemaDataTable {

	protected DepartmentDataTable(final Department schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public DepartmentData getSchemaData(final int idx) {
		return(DepartmentData) super.getSchemaData(idx);
	}
}
