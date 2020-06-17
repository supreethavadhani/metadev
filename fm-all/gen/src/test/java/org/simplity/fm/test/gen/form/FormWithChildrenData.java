package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.Schema1Data;
import java.time.LocalDate;
import java.time.Instant;
/** class for form data formWithChildren  */
public class FormWithChildrenData extends FormData {
	protected FormWithChildrenData(final FormWithChildren form, final Schema1Data dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}

	/**
	 * set value for local1
	 * @param value to be assigned to local1
	 */
	public void setLocal1(String value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of local1
	 */
	public String getLocal1(){
		return super.getStringValue(0);
	}

	/**
	 * set value for local2
	 * @param value to be assigned to local2
	 */
	public void setLocal2(String value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of local2
	 */
	public String getLocal2(){
		return super.getStringValue(0);
	}
}
