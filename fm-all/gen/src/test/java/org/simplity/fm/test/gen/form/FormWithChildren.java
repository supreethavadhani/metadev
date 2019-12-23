package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.Form;
/**
 *
 */
public class FormWithChildren extends Form {
	protected static final String NAME = "formWithChildren";
	protected static final String SCHEMA = "schema1";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
	/**
 * constructor
 */
public FormWithChildren() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}
}
