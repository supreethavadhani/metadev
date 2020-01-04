package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.StudentData;
import java.time.LocalDate;
import java.time.Instant;
/** class for form data studentWithGuardians  */
public class StudentWithGuardiansFd extends FormData {
	protected StudentWithGuardiansFd(final StudentWithGuardians form, final StudentData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}

	/**
	 * set value for levelId
	 * @param value to be assigned to levelId
	 */
	public void setLevelId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of levelId
	 */
	public long getLevelId(){
		return super.getLongValue(0);
	}

	/**
	 * set value for sectionId
	 * @param value to be assigned to sectionId
	 */
	public void setSectionId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of sectionId
	 */
	public long getSectionId(){
		return super.getLongValue(0);
	}
}
