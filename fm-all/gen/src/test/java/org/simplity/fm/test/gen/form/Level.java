package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.test.gen.DefinedDataTypes;
import org.simplity.fm.test.gen.schema.LevelData;
import org.simplity.fm.test.gen.schema.LevelDataTable;
/** class for form level  */
public class Level extends Form {
	protected static final String NAME = "level";
	protected static final String SCHEMA = "level";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Level() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected LevelFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new LevelFd(this, (LevelData) schemaData, values, data);
	}

	@Override
	protected LevelFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new LevelFdt(this, (LevelDataTable) table, values);
	}
}
