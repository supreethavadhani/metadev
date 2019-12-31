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
import org.simplity.fm.test.gen.schema.ProgramData;
import org.simplity.fm.test.gen.schema.ProgramDataTable;
/** class for form program  */
public class Program extends Form {
	protected static final String NAME = "program";
	protected static final String SCHEMA = "program";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Program() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected ProgramData newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new ProgramData(this, (ProgramData) schemaData, values, data);
	}

	@Override
	protected ProgramDataTable newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new ProgramDataTable(this, (ProgramDataTable) table, values);
	}
}
