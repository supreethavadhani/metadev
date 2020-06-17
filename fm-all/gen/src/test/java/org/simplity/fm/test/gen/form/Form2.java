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
import org.simplity.fm.test.gen.schema.Schema1Data;
import org.simplity.fm.test.gen.schema.Schema1DataTable;
/** class for form form2  */
public class Form2 extends Form {
	protected static final String NAME = "form2";
	protected static final String SCHEMA = "schema1";
	protected static final  Field[] FIELDS = {
			new Field("f2", 0, DefinedDataTypes.date, null, null, null, false)};
	protected static final  boolean[] OPS = {true, false, true, false, false, false};
/** constructor */
public Form2() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected Form2Data newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new Form2Data(this, (Schema1Data) schemaData, values, data);
	}

	@Override
	protected Form2DataTable newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new Form2DataTable(this, (Schema1DataTable) table, values);
	}
}
