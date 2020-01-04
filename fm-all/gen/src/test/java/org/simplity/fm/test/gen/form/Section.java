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
import org.simplity.fm.test.gen.schema.SectionData;
import org.simplity.fm.test.gen.schema.SectionDataTable;
/** class for form section  */
public class Section extends Form {
	protected static final String NAME = "section";
	protected static final String SCHEMA = "section";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Section() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected SectionFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new SectionFd(this, (SectionData) schemaData, values, data);
	}

	@Override
	protected SectionFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new SectionFdt(this, (SectionDataTable) table, values);
	}
}
