package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaDataTable;

/**
 * class that represents an array of structure of level
 */ 
public class LevelDataTable extends SchemaDataTable {

	protected LevelDataTable(final Level schema, final Object[][] data) {
		super(schema, data);
	}

	@Override
	public LevelData getSchemaData(final int idx) {
		return(LevelData) super.getSchemaData(idx);
	}
}
