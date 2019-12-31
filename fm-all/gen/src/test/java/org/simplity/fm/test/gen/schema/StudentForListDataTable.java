package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of studentForList
 */ 
public class StudentForListDataTable extends SchemaDataTable {

	protected StudentForListDataTable(final StudentForList schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public StudentForListData getSchemaData(final int idx) {
		return(StudentForListData) super.getSchemaData(idx);
	}
}
