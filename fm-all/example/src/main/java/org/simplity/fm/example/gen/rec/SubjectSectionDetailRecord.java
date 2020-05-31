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
 * class that represents structure of subjectSectionDetail
 */ 
public class SubjectSectionDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("subjectSectionId", 0, DefinedDataTypes.id, null, null, null, "subject_section_id", FieldType.OptionalData), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", FieldType.OptionalData), 
			new DbField("offeredSubjectId", 2, DefinedDataTypes.id, "0", null, null, "offered_subject_id", FieldType.OptionalData), 
			new DbField("subjectId", 3, DefinedDataTypes.id, "0", null, null, "subject_id", FieldType.OptionalData), 
			new DbField("levelSectionId", 4, DefinedDataTypes.id, "0", null, null, "level_section_id", FieldType.OptionalData), 
			new DbField("departmentId", 5, DefinedDataTypes.id, null, null, null, "department_id", FieldType.OptionalData), 
			new DbField("sectionId", 6, DefinedDataTypes.id, null, null, null, "section_id", FieldType.OptionalData), 
			new DbField("subjectName", 7, DefinedDataTypes.name, null, null, null, "subject_name", FieldType.OptionalData), 
			new DbField("subjectCode", 8, DefinedDataTypes.name, null, null, null, "subject_code", FieldType.OptionalData), 
			new DbField("sectionName", 9, DefinedDataTypes.name, null, null, null, "section_name", FieldType.OptionalData), 
			new DbField("totalClasses", 10, DefinedDataTypes.integer, null, null, null, "total_classes", FieldType.OptionalData), 
			new DbField("attendanceFrozen", 11, DefinedDataTypes.bool, "false", null, null, "attendance_frozen", FieldType.OptionalData), 
			new DbField("cieFrozen", 12, DefinedDataTypes.bool, "false", null, null, "cie_frozen", FieldType.OptionalData), 
			new DbField("isOffered", 13, DefinedDataTypes.bool, "false", null, null, "is_offered", FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("subjectSectionDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT subject_section_id, institute_id, offered_subject_id, subject_id, level_section_id, department_id, section_id, subject_name, subject_code, section_name, total_classes, attendance_frozen, cie_frozen, is_offered FROM subject_section_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

	private static final Dba DBA = new Dba(FIELDS, "subject_section_details", SELECT, SELECT_IDX,null, null, null, null, null, null, null);

	/**  default constructor */
	public SubjectSectionDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public SubjectSectionDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public SubjectSectionDetailRecord newInstance(final Object[] values) {
		return new SubjectSectionDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SubjectSectionDetailRecord> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {
		return (List<SubjectSectionDetailRecord>) super.parseTable(inputObject, memberName, forInsert, ctx);
	}

	/**
	 * set value for subjectSectionId
	 * @param value to be assigned to subjectSectionId
	 */
	public void setSubjectSectionId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of subjectSectionId
	 */
	public long getSubjectSectionId(){
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
	 * set value for offeredSubjectId
	 * @param value to be assigned to offeredSubjectId
	 */
	public void setOfferedSubjectId(long value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of offeredSubjectId
	 */
	public long getOfferedSubjectId(){
		return super.fetchLongValue(2);
	}

	/**
	 * set value for subjectId
	 * @param value to be assigned to subjectId
	 */
	public void setSubjectId(long value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of subjectId
	 */
	public long getSubjectId(){
		return super.fetchLongValue(3);
	}

	/**
	 * set value for levelSectionId
	 * @param value to be assigned to levelSectionId
	 */
	public void setLevelSectionId(long value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of levelSectionId
	 */
	public long getLevelSectionId(){
		return super.fetchLongValue(4);
	}

	/**
	 * set value for departmentId
	 * @param value to be assigned to departmentId
	 */
	public void setDepartmentId(long value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of departmentId
	 */
	public long getDepartmentId(){
		return super.fetchLongValue(5);
	}

	/**
	 * set value for sectionId
	 * @param value to be assigned to sectionId
	 */
	public void setSectionId(long value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of sectionId
	 */
	public long getSectionId(){
		return super.fetchLongValue(6);
	}

	/**
	 * set value for subjectName
	 * @param value to be assigned to subjectName
	 */
	public void setSubjectName(String value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of subjectName
	 */
	public String getSubjectName(){
		return super.fetchStringValue(7);
	}

	/**
	 * set value for subjectCode
	 * @param value to be assigned to subjectCode
	 */
	public void setSubjectCode(String value){
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of subjectCode
	 */
	public String getSubjectCode(){
		return super.fetchStringValue(8);
	}

	/**
	 * set value for sectionName
	 * @param value to be assigned to sectionName
	 */
	public void setSectionName(String value){
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of sectionName
	 */
	public String getSectionName(){
		return super.fetchStringValue(9);
	}

	/**
	 * set value for totalClasses
	 * @param value to be assigned to totalClasses
	 */
	public void setTotalClasses(long value){
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of totalClasses
	 */
	public long getTotalClasses(){
		return super.fetchLongValue(10);
	}

	/**
	 * set value for attendanceFrozen
	 * @param value to be assigned to attendanceFrozen
	 */
	public void setAttendanceFrozen(boolean value){
		this.fieldValues[11] = value;
	}

	/**
	 * @return value of attendanceFrozen
	 */
	public boolean getAttendanceFrozen(){
		return super.fetchBoolValue(11);
	}

	/**
	 * set value for cieFrozen
	 * @param value to be assigned to cieFrozen
	 */
	public void setCieFrozen(boolean value){
		this.fieldValues[12] = value;
	}

	/**
	 * @return value of cieFrozen
	 */
	public boolean getCieFrozen(){
		return super.fetchBoolValue(12);
	}

	/**
	 * set value for isOffered
	 * @param value to be assigned to isOffered
	 */
	public void setIsOffered(boolean value){
		this.fieldValues[13] = value;
	}

	/**
	 * @return value of isOffered
	 */
	public boolean getIsOffered(){
		return super.fetchBoolValue(13);
	}
}
