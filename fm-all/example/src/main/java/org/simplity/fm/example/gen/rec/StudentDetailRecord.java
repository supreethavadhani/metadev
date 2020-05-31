package org.simplity.fm.example.gen.rec;

import java.time.LocalDate;
import java.time.Instant;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.service.IServiceContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of studentDetail
 */ 
public class StudentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentId", 0, DefinedDataTypes.flexibleId, "-1", null, null, "student_id", FieldType.OptionalData), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", FieldType.OptionalData), 
			new DbField("departmentId", 2, DefinedDataTypes.id, null, null, "departmentList", "department_id", FieldType.OptionalData), 
			new DbField("departmentName", 3, DefinedDataTypes.text, null, null, null, "department_name", FieldType.OptionalData), 
			new DbField("usn", 4, DefinedDataTypes.text, null, null, null, "usn", FieldType.OptionalData), 
			new DbField("name", 5, DefinedDataTypes.name, null, null, null, "name", FieldType.OptionalData), 
			new DbField("phoneNumber", 6, DefinedDataTypes.phone, null, null, null, "phone_number", FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("studentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT student_id, institute_id, department_id, department_name, usn, name, phone_number FROM student_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6};

	private static final Dba DBA = new Dba(FIELDS, "student_details", SELECT, SELECT_IDX,null, null, null, null, null, null, null);

	/**  default constructor */
	public StudentDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public StudentDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentDetailRecord newInstance(final Object[] values) {
		return new StudentDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudentDetailRecord> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {
		return (List<StudentDetailRecord>) super.parseTable(inputObject, memberName, forInsert, ctx);
	}

	/**
	 * set value for studentId
	 * @param value to be assigned to studentId
	 */
	public void setStudentId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of studentId
	 */
	public long getStudentId(){
		return super.fetchLongValue(0);
	}

	/**
	 * set value for instituteId
	 * @param value to be assigned to instituteId
	 */
	public void setInstituteId(long value){
		this.fieldValues[1] = value;
	}

	/**
	 * @return value of instituteId
	 */
	public long getInstituteId(){
		return super.fetchLongValue(1);
	}

	/**
	 * set value for departmentId
	 * @param value to be assigned to departmentId
	 */
	public void setDepartmentId(long value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of departmentId
	 */
	public long getDepartmentId(){
		return super.fetchLongValue(2);
	}

	/**
	 * set value for departmentName
	 * @param value to be assigned to departmentName
	 */
	public void setDepartmentName(String value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of departmentName
	 */
	public String getDepartmentName(){
		return super.fetchStringValue(3);
	}

	/**
	 * set value for usn
	 * @param value to be assigned to usn
	 */
	public void setUsn(String value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn(){
		return super.fetchStringValue(4);
	}

	/**
	 * set value for name
	 * @param value to be assigned to name
	 */
	public void setName(String value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName(){
		return super.fetchStringValue(5);
	}

	/**
	 * set value for phoneNumber
	 * @param value to be assigned to phoneNumber
	 */
	public void setPhoneNumber(String value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of phoneNumber
	 */
	public String getPhoneNumber(){
		return super.fetchStringValue(6);
	}
}
