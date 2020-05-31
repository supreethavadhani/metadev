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
 * class that represents structure of studentCieDetail
 */ 
public class StudentCieDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentId", 0, DefinedDataTypes.flexibleId, "-1", null, null, "student_id", FieldType.OptionalData), 
			new DbField("subjectSectionId", 1, DefinedDataTypes.id, null, null, null, "subject_section_id", FieldType.OptionalData), 
			new DbField("departmentName", 2, DefinedDataTypes.text, null, null, null, "department_name", FieldType.OptionalData), 
			new DbField("usn", 3, DefinedDataTypes.text, null, null, null, "usn", FieldType.OptionalData), 
			new DbField("name", 4, DefinedDataTypes.name, null, null, null, "student_name", FieldType.OptionalData), 
			new DbField("eligibility", 5, DefinedDataTypes.name, null, null, null, "is_eligible", FieldType.OptionalData), 
			new DbField("test1", 6, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("test2", 7, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("test3", 8, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("quiz1", 9, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("quiz2", 10, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("theoryCie", 11, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("theoryClassesHeld", 12, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("theoryClassesAttended", 13, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("theoryClassesPercentage", 14, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("practicalCie", 15, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("practicalClassesHeld", 16, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("practicalClassesAttended", 17, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("practicalClassesPercentage", 18, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("selfStudy", 19, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("practicalMarks", 20, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData), 
			new DbField("totalCie", 21, DefinedDataTypes.text, "1", null, null, null, FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("studentCieDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT student_id, subject_section_id, department_name, usn, student_name, is_eligible, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null FROM student_cie_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

	private static final Dba DBA = new Dba(FIELDS, "student_cie_details", SELECT, SELECT_IDX,null, null, null, null, null, null, null);

	/**  default constructor */
	public StudentCieDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public StudentCieDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentCieDetailRecord newInstance(final Object[] values) {
		return new StudentCieDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudentCieDetailRecord> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {
		return (List<StudentCieDetailRecord>) super.parseTable(inputObject, memberName, forInsert, ctx);
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
	 * set value for subjectSectionId
	 * @param value to be assigned to subjectSectionId
	 */
	public void setSubjectSectionId(long value){
		this.fieldValues[1] = value;
	}

	/**
	 * @return value of subjectSectionId
	 */
	public long getSubjectSectionId(){
		return super.fetchLongValue(1);
	}

	/**
	 * set value for departmentName
	 * @param value to be assigned to departmentName
	 */
	public void setDepartmentName(String value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of departmentName
	 */
	public String getDepartmentName(){
		return super.fetchStringValue(2);
	}

	/**
	 * set value for usn
	 * @param value to be assigned to usn
	 */
	public void setUsn(String value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn(){
		return super.fetchStringValue(3);
	}

	/**
	 * set value for name
	 * @param value to be assigned to name
	 */
	public void setName(String value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName(){
		return super.fetchStringValue(4);
	}

	/**
	 * set value for eligibility
	 * @param value to be assigned to eligibility
	 */
	public void setEligibility(String value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of eligibility
	 */
	public String getEligibility(){
		return super.fetchStringValue(5);
	}

	/**
	 * set value for test1
	 * @param value to be assigned to test1
	 */
	public void setTest1(String value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of test1
	 */
	public String getTest1(){
		return super.fetchStringValue(6);
	}

	/**
	 * set value for test2
	 * @param value to be assigned to test2
	 */
	public void setTest2(String value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of test2
	 */
	public String getTest2(){
		return super.fetchStringValue(7);
	}

	/**
	 * set value for test3
	 * @param value to be assigned to test3
	 */
	public void setTest3(String value){
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of test3
	 */
	public String getTest3(){
		return super.fetchStringValue(8);
	}

	/**
	 * set value for quiz1
	 * @param value to be assigned to quiz1
	 */
	public void setQuiz1(String value){
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of quiz1
	 */
	public String getQuiz1(){
		return super.fetchStringValue(9);
	}

	/**
	 * set value for quiz2
	 * @param value to be assigned to quiz2
	 */
	public void setQuiz2(String value){
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of quiz2
	 */
	public String getQuiz2(){
		return super.fetchStringValue(10);
	}

	/**
	 * set value for theoryCie
	 * @param value to be assigned to theoryCie
	 */
	public void setTheoryCie(String value){
		this.fieldValues[11] = value;
	}

	/**
	 * @return value of theoryCie
	 */
	public String getTheoryCie(){
		return super.fetchStringValue(11);
	}

	/**
	 * set value for theoryClassesHeld
	 * @param value to be assigned to theoryClassesHeld
	 */
	public void setTheoryClassesHeld(String value){
		this.fieldValues[12] = value;
	}

	/**
	 * @return value of theoryClassesHeld
	 */
	public String getTheoryClassesHeld(){
		return super.fetchStringValue(12);
	}

	/**
	 * set value for theoryClassesAttended
	 * @param value to be assigned to theoryClassesAttended
	 */
	public void setTheoryClassesAttended(String value){
		this.fieldValues[13] = value;
	}

	/**
	 * @return value of theoryClassesAttended
	 */
	public String getTheoryClassesAttended(){
		return super.fetchStringValue(13);
	}

	/**
	 * set value for theoryClassesPercentage
	 * @param value to be assigned to theoryClassesPercentage
	 */
	public void setTheoryClassesPercentage(String value){
		this.fieldValues[14] = value;
	}

	/**
	 * @return value of theoryClassesPercentage
	 */
	public String getTheoryClassesPercentage(){
		return super.fetchStringValue(14);
	}

	/**
	 * set value for practicalCie
	 * @param value to be assigned to practicalCie
	 */
	public void setPracticalCie(String value){
		this.fieldValues[15] = value;
	}

	/**
	 * @return value of practicalCie
	 */
	public String getPracticalCie(){
		return super.fetchStringValue(15);
	}

	/**
	 * set value for practicalClassesHeld
	 * @param value to be assigned to practicalClassesHeld
	 */
	public void setPracticalClassesHeld(String value){
		this.fieldValues[16] = value;
	}

	/**
	 * @return value of practicalClassesHeld
	 */
	public String getPracticalClassesHeld(){
		return super.fetchStringValue(16);
	}

	/**
	 * set value for practicalClassesAttended
	 * @param value to be assigned to practicalClassesAttended
	 */
	public void setPracticalClassesAttended(String value){
		this.fieldValues[17] = value;
	}

	/**
	 * @return value of practicalClassesAttended
	 */
	public String getPracticalClassesAttended(){
		return super.fetchStringValue(17);
	}

	/**
	 * set value for practicalClassesPercentage
	 * @param value to be assigned to practicalClassesPercentage
	 */
	public void setPracticalClassesPercentage(String value){
		this.fieldValues[18] = value;
	}

	/**
	 * @return value of practicalClassesPercentage
	 */
	public String getPracticalClassesPercentage(){
		return super.fetchStringValue(18);
	}

	/**
	 * set value for selfStudy
	 * @param value to be assigned to selfStudy
	 */
	public void setSelfStudy(String value){
		this.fieldValues[19] = value;
	}

	/**
	 * @return value of selfStudy
	 */
	public String getSelfStudy(){
		return super.fetchStringValue(19);
	}

	/**
	 * set value for practicalMarks
	 * @param value to be assigned to practicalMarks
	 */
	public void setPracticalMarks(String value){
		this.fieldValues[20] = value;
	}

	/**
	 * @return value of practicalMarks
	 */
	public String getPracticalMarks(){
		return super.fetchStringValue(20);
	}

	/**
	 * set value for totalCie
	 * @param value to be assigned to totalCie
	 */
	public void setTotalCie(String value){
		this.fieldValues[21] = value;
	}

	/**
	 * @return value of totalCie
	 */
	public String getTotalCie(){
		return super.fetchStringValue(21);
	}
}
