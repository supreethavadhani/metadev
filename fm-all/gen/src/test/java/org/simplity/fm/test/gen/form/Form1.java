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
/** class for form form1  */
public class Form1 extends Form {
	protected static final String NAME = "form1";
	protected static final String SCHEMA = "schema1";
	protected static final  Field[] FIELDS = {
			new Field("f1", 0, DefinedDataTypes.text, null, null, null, false)};
	protected static final  boolean[] OPS = {true, false, true, false, false, false};
/** constructor */
public Form1() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected Form1Data newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new Form1Data(this, (Schema1Data) schemaData, values, data);
	}

	@Override
	protected Form1DataTable newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new Form1DataTable(this, (Schema1DataTable) table, values);
	}
}
