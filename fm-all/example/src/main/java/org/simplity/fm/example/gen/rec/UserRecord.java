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
import java.util.List;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of user
 */ 
public class UserRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("userId", 0, DefinedDataTypes.flexibleId, null, null, null, "user_id", FieldType.OptionalData), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", FieldType.OptionalData), 
			new DbField("trustId", 2, DefinedDataTypes.id, null, null, null, "trust_id", FieldType.OptionalData), 
			new DbField("userType", 3, DefinedDataTypes.userType, "Student", null, "userType", "user_type", FieldType.OptionalData), 
			new DbField("loginId", 4, DefinedDataTypes.loginId, null, null, null, "login_id", FieldType.OptionalData), 
			new DbField("password", 5, DefinedDataTypes.text, null, null, null, "password", FieldType.OptionalData), 
			new DbField("loginEnabled", 6, DefinedDataTypes.bool, "false", null, null, "login_enabled", FieldType.OptionalData), 
			new DbField("previousLoginAt", 7, DefinedDataTypes.timestamp, null, null, null, "previous_login_at", FieldType.OptionalData), 
			new DbField("currentLoginAt", 8, DefinedDataTypes.timestamp, null, null, null, "current_login_at", FieldType.OptionalData), 
			new DbField("resetPasswordCount", 9, DefinedDataTypes.integer, null, null, null, "reset_password_count", FieldType.OptionalData), 
			new DbField("resetPasswordSentAt", 10, DefinedDataTypes.timestamp, null, null, null, "reset_password_sent_at", FieldType.OptionalData), 
			new DbField("currentLoginIp", 11, DefinedDataTypes.ip, null, null, null, "current_login_ip", FieldType.OptionalData), 
			new DbField("previousLoginIp", 12, DefinedDataTypes.ip, null, null, null, "previous_login_ip", FieldType.OptionalData), 
			new DbField("loginCount", 13, DefinedDataTypes.integer, null, null, null, "login_count", FieldType.OptionalData), 
			new DbField("confirmationToken", 14, DefinedDataTypes.text, null, null, null, "confirmation_token", FieldType.OptionalData), 
			new DbField("loginToken", 15, DefinedDataTypes.text, null, null, null, "login_token", FieldType.OptionalData), 
			new DbField("createdAt", 16, DefinedDataTypes.timestamp, null, null, null, "created_at", FieldType.OptionalData), 
			new DbField("createdBy", 17, DefinedDataTypes.id, null, null, null, "created_by", FieldType.OptionalData), 
			new DbField("updatedAt", 18, DefinedDataTypes.timestamp, null, null, null, "updated_at", FieldType.OptionalData), 
			new DbField("updatedBy", 19, DefinedDataTypes.id, null, null, null, "updated_by", FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("user", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT user_id, institute_id, trust_id, user_type, login_id, password, login_enabled, previous_login_at, current_login_at, reset_password_count, reset_password_sent_at, current_login_ip, previous_login_ip, login_count, confirmation_token, login_token, created_at, created_by, updated_at, updated_by FROM users";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};

	private static final Dba DBA = new Dba(FIELDS, "users", SELECT, SELECT_IDX,null, null, null, null, null, null, null);

	/**  default constructor */
	public UserRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public UserRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public UserRecord newInstance(final Object[] values) {
		return new UserRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UserRecord> parseTable(final IInputObject inputObject, String memberName, final boolean forInsert, final IServiceContext ctx) {
		return (List<UserRecord>) super.parseTable(inputObject, memberName, forInsert, ctx);
	}

	/**
	 * set value for userId
	 * @param value to be assigned to userId
	 */
	public void setUserId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of userId
	 */
	public long getUserId(){
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
	 * set value for trustId
	 * @param value to be assigned to trustId
	 */
	public void setTrustId(long value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of trustId
	 */
	public long getTrustId(){
		return super.fetchLongValue(2);
	}

	/**
	 * set value for userType
	 * @param value to be assigned to userType
	 */
	public void setUserType(String value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of userType
	 */
	public String getUserType(){
		return super.fetchStringValue(3);
	}

	/**
	 * set value for loginId
	 * @param value to be assigned to loginId
	 */
	public void setLoginId(String value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of loginId
	 */
	public String getLoginId(){
		return super.fetchStringValue(4);
	}

	/**
	 * set value for password
	 * @param value to be assigned to password
	 */
	public void setPassword(String value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of password
	 */
	public String getPassword(){
		return super.fetchStringValue(5);
	}

	/**
	 * set value for loginEnabled
	 * @param value to be assigned to loginEnabled
	 */
	public void setLoginEnabled(boolean value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of loginEnabled
	 */
	public boolean getLoginEnabled(){
		return super.fetchBoolValue(6);
	}

	/**
	 * set value for previousLoginAt
	 * @param value to be assigned to previousLoginAt
	 */
	public void setPreviousLoginAt(Instant value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of previousLoginAt
	 */
	public Instant getPreviousLoginAt(){
		return super.fetchTimestampValue(7);
	}

	/**
	 * set value for currentLoginAt
	 * @param value to be assigned to currentLoginAt
	 */
	public void setCurrentLoginAt(Instant value){
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of currentLoginAt
	 */
	public Instant getCurrentLoginAt(){
		return super.fetchTimestampValue(8);
	}

	/**
	 * set value for resetPasswordCount
	 * @param value to be assigned to resetPasswordCount
	 */
	public void setResetPasswordCount(long value){
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of resetPasswordCount
	 */
	public long getResetPasswordCount(){
		return super.fetchLongValue(9);
	}

	/**
	 * set value for resetPasswordSentAt
	 * @param value to be assigned to resetPasswordSentAt
	 */
	public void setResetPasswordSentAt(Instant value){
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of resetPasswordSentAt
	 */
	public Instant getResetPasswordSentAt(){
		return super.fetchTimestampValue(10);
	}

	/**
	 * set value for currentLoginIp
	 * @param value to be assigned to currentLoginIp
	 */
	public void setCurrentLoginIp(String value){
		this.fieldValues[11] = value;
	}

	/**
	 * @return value of currentLoginIp
	 */
	public String getCurrentLoginIp(){
		return super.fetchStringValue(11);
	}

	/**
	 * set value for previousLoginIp
	 * @param value to be assigned to previousLoginIp
	 */
	public void setPreviousLoginIp(String value){
		this.fieldValues[12] = value;
	}

	/**
	 * @return value of previousLoginIp
	 */
	public String getPreviousLoginIp(){
		return super.fetchStringValue(12);
	}

	/**
	 * set value for loginCount
	 * @param value to be assigned to loginCount
	 */
	public void setLoginCount(long value){
		this.fieldValues[13] = value;
	}

	/**
	 * @return value of loginCount
	 */
	public long getLoginCount(){
		return super.fetchLongValue(13);
	}

	/**
	 * set value for confirmationToken
	 * @param value to be assigned to confirmationToken
	 */
	public void setConfirmationToken(String value){
		this.fieldValues[14] = value;
	}

	/**
	 * @return value of confirmationToken
	 */
	public String getConfirmationToken(){
		return super.fetchStringValue(14);
	}

	/**
	 * set value for loginToken
	 * @param value to be assigned to loginToken
	 */
	public void setLoginToken(String value){
		this.fieldValues[15] = value;
	}

	/**
	 * @return value of loginToken
	 */
	public String getLoginToken(){
		return super.fetchStringValue(15);
	}

	/**
	 * set value for createdAt
	 * @param value to be assigned to createdAt
	 */
	public void setCreatedAt(Instant value){
		this.fieldValues[16] = value;
	}

	/**
	 * @return value of createdAt
	 */
	public Instant getCreatedAt(){
		return super.fetchTimestampValue(16);
	}

	/**
	 * set value for createdBy
	 * @param value to be assigned to createdBy
	 */
	public void setCreatedBy(long value){
		this.fieldValues[17] = value;
	}

	/**
	 * @return value of createdBy
	 */
	public long getCreatedBy(){
		return super.fetchLongValue(17);
	}

	/**
	 * set value for updatedAt
	 * @param value to be assigned to updatedAt
	 */
	public void setUpdatedAt(Instant value){
		this.fieldValues[18] = value;
	}

	/**
	 * @return value of updatedAt
	 */
	public Instant getUpdatedAt(){
		return super.fetchTimestampValue(18);
	}

	/**
	 * set value for updatedBy
	 * @param value to be assigned to updatedBy
	 */
	public void setUpdatedBy(long value){
		this.fieldValues[19] = value;
	}

	/**
	 * @return value of updatedBy
	 */
	public long getUpdatedBy(){
		return super.fetchLongValue(19);
	}
}
