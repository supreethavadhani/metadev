package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaData;
import java.time.Instant;
import java.time.LocalDate;

/**
 * class that represents structure of student
 */ 
public class StudentData extends SchemaData {

	protected StudentData(final Student schema, final Object[] data) {
		super(schema, data);
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
		return super.getLongValue(0);
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
		return super.getLongValue(1);
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
		return super.getLongValue(2);
	}

	/**
	 * set value for programId
	 * @param value to be assigned to programId
	 */
	public void setProgramId(long value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of programId
	 */
	public long getProgramId(){
		return super.getLongValue(3);
	}

	/**
	 * set value for tempUsn
	 * @param value to be assigned to tempUsn
	 */
	public void setTempUsn(String value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of tempUsn
	 */
	public String getTempUsn(){
		return super.getStringValue(4);
	}

	/**
	 * set value for usn
	 * @param value to be assigned to usn
	 */
	public void setUsn(String value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn(){
		return super.getStringValue(5);
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
		return super.getStringValue(6);
	}

	/**
	 * set value for presentAddressLine1
	 * @param value to be assigned to presentAddressLine1
	 */
	public void setPresentAddressLine1(String value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of presentAddressLine1
	 */
	public String getPresentAddressLine1(){
		return super.getStringValue(7);
	}

	/**
	 * set value for presentAddressLine2
	 * @param value to be assigned to presentAddressLine2
	 */
	public void setPresentAddressLine2(String value){
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of presentAddressLine2
	 */
	public String getPresentAddressLine2(){
		return super.getStringValue(8);
	}

	/**
	 * set value for presentCity
	 * @param value to be assigned to presentCity
	 */
	public void setPresentCity(String value){
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of presentCity
	 */
	public String getPresentCity(){
		return super.getStringValue(9);
	}

	/**
	 * set value for presentState
	 * @param value to be assigned to presentState
	 */
	public void setPresentState(String value){
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of presentState
	 */
	public String getPresentState(){
		return super.getStringValue(10);
	}

	/**
	 * set value for presentPincode
	 * @param value to be assigned to presentPincode
	 */
	public void setPresentPincode(String value){
		this.fieldValues[11] = value;
	}

	/**
	 * @return value of presentPincode
	 */
	public String getPresentPincode(){
		return super.getStringValue(11);
	}

	/**
	 * set value for presentCountry
	 * @param value to be assigned to presentCountry
	 */
	public void setPresentCountry(long value){
		this.fieldValues[12] = value;
	}

	/**
	 * @return value of presentCountry
	 */
	public long getPresentCountry(){
		return super.getLongValue(12);
	}

	/**
	 * set value for addressLine1
	 * @param value to be assigned to addressLine1
	 */
	public void setAddressLine1(String value){
		this.fieldValues[13] = value;
	}

	/**
	 * @return value of addressLine1
	 */
	public String getAddressLine1(){
		return super.getStringValue(13);
	}

	/**
	 * set value for addressLine2
	 * @param value to be assigned to addressLine2
	 */
	public void setAddressLine2(String value){
		this.fieldValues[14] = value;
	}

	/**
	 * @return value of addressLine2
	 */
	public String getAddressLine2(){
		return super.getStringValue(14);
	}

	/**
	 * set value for city
	 * @param value to be assigned to city
	 */
	public void setCity(String value){
		this.fieldValues[15] = value;
	}

	/**
	 * @return value of city
	 */
	public String getCity(){
		return super.getStringValue(15);
	}

	/**
	 * set value for state
	 * @param value to be assigned to state
	 */
	public void setState(String value){
		this.fieldValues[16] = value;
	}

	/**
	 * @return value of state
	 */
	public String getState(){
		return super.getStringValue(16);
	}

	/**
	 * set value for pincode
	 * @param value to be assigned to pincode
	 */
	public void setPincode(String value){
		this.fieldValues[17] = value;
	}

	/**
	 * @return value of pincode
	 */
	public String getPincode(){
		return super.getStringValue(17);
	}

	/**
	 * set value for country
	 * @param value to be assigned to country
	 */
	public void setCountry(long value){
		this.fieldValues[18] = value;
	}

	/**
	 * @return value of country
	 */
	public long getCountry(){
		return super.getLongValue(18);
	}

	/**
	 * set value for phoneNumber
	 * @param value to be assigned to phoneNumber
	 */
	public void setPhoneNumber(String value){
		this.fieldValues[19] = value;
	}

	/**
	 * @return value of phoneNumber
	 */
	public String getPhoneNumber(){
		return super.getStringValue(19);
	}

	/**
	 * set value for email
	 * @param value to be assigned to email
	 */
	public void setEmail(String value){
		this.fieldValues[20] = value;
	}

	/**
	 * @return value of email
	 */
	public String getEmail(){
		return super.getStringValue(20);
	}

	/**
	 * set value for gender
	 * @param value to be assigned to gender
	 */
	public void setGender(String value){
		this.fieldValues[21] = value;
	}

	/**
	 * @return value of gender
	 */
	public String getGender(){
		return super.getStringValue(21);
	}

	/**
	 * set value for admissionQuota
	 * @param value to be assigned to admissionQuota
	 */
	public void setAdmissionQuota(String value){
		this.fieldValues[22] = value;
	}

	/**
	 * @return value of admissionQuota
	 */
	public String getAdmissionQuota(){
		return super.getStringValue(22);
	}

	/**
	 * set value for admittedLevel
	 * @param value to be assigned to admittedLevel
	 */
	public void setAdmittedLevel(String value){
		this.fieldValues[23] = value;
	}

	/**
	 * @return value of admittedLevel
	 */
	public String getAdmittedLevel(){
		return super.getStringValue(23);
	}

	/**
	 * set value for admissionDate
	 * @param value to be assigned to admissionDate
	 */
	public void setAdmissionDate(LocalDate value){
		this.fieldValues[24] = value;
	}

	/**
	 * @return value of admissionDate
	 */
	public LocalDate getAdmissionDate(){
		return super.getDateValue(24);
	}

	/**
	 * set value for bloodGroup
	 * @param value to be assigned to bloodGroup
	 */
	public void setBloodGroup(String value){
		this.fieldValues[25] = value;
	}

	/**
	 * @return value of bloodGroup
	 */
	public String getBloodGroup(){
		return super.getStringValue(25);
	}

	/**
	 * set value for religion
	 * @param value to be assigned to religion
	 */
	public void setReligion(String value){
		this.fieldValues[26] = value;
	}

	/**
	 * @return value of religion
	 */
	public String getReligion(){
		return super.getStringValue(26);
	}

	/**
	 * set value for caste
	 * @param value to be assigned to caste
	 */
	public void setCaste(String value){
		this.fieldValues[27] = value;
	}

	/**
	 * @return value of caste
	 */
	public String getCaste(){
		return super.getStringValue(27);
	}

	/**
	 * set value for nationality
	 * @param value to be assigned to nationality
	 */
	public void setNationality(String value){
		this.fieldValues[28] = value;
	}

	/**
	 * @return value of nationality
	 */
	public String getNationality(){
		return super.getStringValue(28);
	}

	/**
	 * set value for category
	 * @param value to be assigned to category
	 */
	public void setCategory(String value){
		this.fieldValues[29] = value;
	}

	/**
	 * @return value of category
	 */
	public String getCategory(){
		return super.getStringValue(29);
	}

	/**
	 * set value for personalId
	 * @param value to be assigned to personalId
	 */
	public void setPersonalId(String value){
		this.fieldValues[30] = value;
	}

	/**
	 * @return value of personalId
	 */
	public String getPersonalId(){
		return super.getStringValue(30);
	}

	/**
	 * set value for dateOfBirth
	 * @param value to be assigned to dateOfBirth
	 */
	public void setDateOfBirth(LocalDate value){
		this.fieldValues[31] = value;
	}

	/**
	 * @return value of dateOfBirth
	 */
	public LocalDate getDateOfBirth(){
		return super.getDateValue(31);
	}

	/**
	 * set value for placeOfBirth
	 * @param value to be assigned to placeOfBirth
	 */
	public void setPlaceOfBirth(String value){
		this.fieldValues[32] = value;
	}

	/**
	 * @return value of placeOfBirth
	 */
	public String getPlaceOfBirth(){
		return super.getStringValue(32);
	}

	/**
	 * set value for domicileState
	 * @param value to be assigned to domicileState
	 */
	public void setDomicileState(String value){
		this.fieldValues[33] = value;
	}

	/**
	 * @return value of domicileState
	 */
	public String getDomicileState(){
		return super.getStringValue(33);
	}

	/**
	 * set value for previousBoard
	 * @param value to be assigned to previousBoard
	 */
	public void setPreviousBoard(String value){
		this.fieldValues[34] = value;
	}

	/**
	 * @return value of previousBoard
	 */
	public String getPreviousBoard(){
		return super.getStringValue(34);
	}

	/**
	 * set value for previousClass
	 * @param value to be assigned to previousClass
	 */
	public void setPreviousClass(String value){
		this.fieldValues[35] = value;
	}

	/**
	 * @return value of previousClass
	 */
	public String getPreviousClass(){
		return super.getStringValue(35);
	}

	/**
	 * set value for previousInstitute
	 * @param value to be assigned to previousInstitute
	 */
	public void setPreviousInstitute(String value){
		this.fieldValues[36] = value;
	}

	/**
	 * @return value of previousInstitute
	 */
	public String getPreviousInstitute(){
		return super.getStringValue(36);
	}

	/**
	 * set value for qualifyingExamRank
	 * @param value to be assigned to qualifyingExamRank
	 */
	public void setQualifyingExamRank(String value){
		this.fieldValues[37] = value;
	}

	/**
	 * @return value of qualifyingExamRank
	 */
	public String getQualifyingExamRank(){
		return super.getStringValue(37);
	}

	/**
	 * set value for createdAt
	 * @param value to be assigned to createdAt
	 */
	public void setCreatedAt(Instant value){
		this.fieldValues[38] = value;
	}

	/**
	 * @return value of createdAt
	 */
	public Instant getCreatedAt(){
		return super.getTimestampValue(38);
	}

	/**
	 * set value for updatedAt
	 * @param value to be assigned to updatedAt
	 */
	public void setUpdatedAt(Instant value){
		this.fieldValues[39] = value;
	}

	/**
	 * @return value of updatedAt
	 */
	public Instant getUpdatedAt(){
		return super.getTimestampValue(39);
	}
}
