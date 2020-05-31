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
 * class that represents structure of studentAssessmentDetail
 */ 
public class StudentAssessmentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentAssessmentId", 0, DefinedDataTypes.id, null, null, null, "student_assessment_id", FieldType.OptionalData), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", FieldType.OptionalData), 
			new DbField("subjectSectionId", 2, DefinedDataTypes.id, null, null, null, "subject_section_id", FieldType.OptionalData), 
			new DbField("assessmentSchemeId", 3, DefinedDataTypes.id, null, null, null, "assessment_scheme_id", FieldType.OptionalData), 
			new DbField("assessmentSeqNo", 4, DefinedDataTypes.integer, null, null, null, "assessment_seq_no", FieldType.OptionalData), 
			new DbField("studentId", 5, DefinedDataTypes.id, null, null, null, "student_id", FieldType.OptionalData), 
			new DbField("name", 6, DefinedDataTypes.name, null, null, null, "name", FieldType.OptionalData), 
			new DbField("usn", 7, DefinedDataTypes.text, null, null, null, "usn", FieldType.OptionalData), 
			new DbField("hasAttended", 8, DefinedDataTypes.bool, "false", null, null, "has_attended", FieldType.OptionalData), 
			new DbField("marksScored", 9, DefinedDataTypes.integer, "0", null, null, "marks_scored", FieldType.OptionalData), 
			new DbField("marks", 10, DefinedDataTypes.text, null, null, null, null, FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("studentAssessmentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT student_assessment_id, institute_id, subject_section_id, assessment_scheme_id, assessment_seq_no, student_id, name, usn, has_attended, marks_scored, null FROM student_assessment_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

	private static final Dba DBA = new Dba(FIELDS, "student_assessment_details", SELECT, SELECT_IDX,null, null, null, null, null, null, null);

	/**  default constructor */
	public StudentAssessmentDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public StudentAssessmentDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentAssessmentDetailRecord newInstance(final Object[] values) {
		return new StudentAssessmentDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudentAssessmentDetailRecord> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {
		return (List<StudentAssessmentDetailRecord>) super.parseTable(inputObject, memberName, forInsert, ctx);
	}

	/**
	 * set value for studentAssessmentId
	 * @param value to be assigned to studentAssessmentId
	 */
	public void setStudentAssessmentId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of studentAssessmentId
	 */
	public long getStudentAssessmentId(){
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
	 * set value for subjectSectionId
	 * @param value to be assigned to subjectSectionId
	 */
	public void setSubjectSectionId(long value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of subjectSectionId
	 */
	public long getSubjectSectionId(){
		return super.fetchLongValue(2);
	}

	/**
	 * set value for assessmentSchemeId
	 * @param value to be assigned to assessmentSchemeId
	 */
	public void setAssessmentSchemeId(long value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of assessmentSchemeId
	 */
	public long getAssessmentSchemeId(){
		return super.fetchLongValue(3);
	}

	/**
	 * set value for assessmentSeqNo
	 * @param value to be assigned to assessmentSeqNo
	 */
	public void setAssessmentSeqNo(long value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of assessmentSeqNo
	 */
	public long getAssessmentSeqNo(){
		return super.fetchLongValue(4);
	}

	/**
	 * set value for studentId
	 * @param value to be assigned to studentId
	 */
	public void setStudentId(long value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of studentId
	 */
	public long getStudentId(){
		return super.fetchLongValue(5);
	}

	/**
	 * set value for name
	 * @param value to be assigned to name
	 */
	public void setName(String value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName(){
		return super.fetchStringValue(6);
	}

	/**
	 * set value for usn
	 * @param value to be assigned to usn
	 */
	public void setUsn(String value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn(){
		return super.fetchStringValue(7);
	}

	/**
	 * set value for hasAttended
	 * @param value to be assigned to hasAttended
	 */
	public void setHasAttended(boolean value){
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of hasAttended
	 */
	public boolean getHasAttended(){
		return super.fetchBoolValue(8);
	}

	/**
	 * set value for marksScored
	 * @param value to be assigned to marksScored
	 */
	public void setMarksScored(long value){
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of marksScored
	 */
	public long getMarksScored(){
		return super.fetchLongValue(9);
	}

	/**
	 * set value for marks
	 * @param value to be assigned to marks
	 */
	public void setMarks(String value){
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of marks
	 */
	public String getMarks(){
		return super.fetchStringValue(10);
	}
}
