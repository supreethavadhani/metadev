package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.Schema1Data;
import java.time.LocalDate;
import java.time.Instant;
/** class for form data form1  */
public class Form1Data extends FormData {
	protected Form1Data(final Form1 form, final Schema1Data dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}

	/**
	 * set value for f1
	 * @param value to be assigned to f1
	 */
	public void setF1(String value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of f1
	 */
	public String getF1(){
		return super.getStringValue(0);
	}
}
