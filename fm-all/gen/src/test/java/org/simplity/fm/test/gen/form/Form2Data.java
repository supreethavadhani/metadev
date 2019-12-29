package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.Schema1Data;
import java.time.LocalDate;
import java.time.Instant;
/** class for form data form2  */
public class Form2Data extends FormData {
	protected Form2Data(final Form2 form, final Schema1Data dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}

	/**
	 * set value for f2
	 * @param value to be assigned to f2
	 */
	public void setF2(LocalDate value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of f2
	 */
	public LocalDate getF2(){
		return super.getDateValue(0);
	}
}
