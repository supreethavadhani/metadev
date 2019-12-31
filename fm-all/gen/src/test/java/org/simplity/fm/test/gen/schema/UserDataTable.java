package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of user
 */ 
public class UserDataTable extends SchemaDataTable {

	protected UserDataTable(final User schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public UserData getSchemaData(final int idx) {
		return(UserData) super.getSchemaData(idx);
	}
}
