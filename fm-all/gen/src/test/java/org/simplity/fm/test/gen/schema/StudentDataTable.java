package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of student
 */ 
public class StudentDataTable extends SchemaDataTable {

	protected StudentDataTable(final Student schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public StudentData getSchemaData(final int idx) {
		return(StudentData) super.getSchemaData(idx);
	}
}
