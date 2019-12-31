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
import org.simplity.fm.test.gen.schema.InstituteData;
import org.simplity.fm.test.gen.schema.InstituteDataTable;
/** class for form institute  */
public class Institute extends Form {
	protected static final String NAME = "institute";
	protected static final String SCHEMA = "institute";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Institute() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected InstituteData newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new InstituteData(this, (InstituteData) schemaData, values, data);
	}

	@Override
	protected InstituteDataTable newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new InstituteDataTable(this, (InstituteDataTable) table, values);
	}
}
