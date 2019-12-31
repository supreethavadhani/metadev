package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaData;
import java.time.Instant;
import java.time.LocalDate;

/**
 * class that represents structure of user
 */ 
public class UserData extends SchemaData {

	protected UserData(final User schema, final Object[] data) {
		super(schema, data);
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
		return super.getLongValue(2);
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
		return super.getStringValue(3);
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
		return super.getStringValue(4);
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
		return super.getStringValue(5);
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
		return super.getBoolValue(6);
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
		return super.getTimestampValue(7);
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
		return super.getTimestampValue(8);
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
		return super.getLongValue(9);
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
		return super.getTimestampValue(10);
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
		return super.getStringValue(11);
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
		return super.getStringValue(12);
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
		return super.getLongValue(13);
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
		return super.getStringValue(14);
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
		return super.getStringValue(15);
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
		return super.getTimestampValue(16);
	}

	/**
	 * set value for updatedAt
	 * @param value to be assigned to updatedAt
	 */
	public void setUpdatedAt(Instant value){
		this.fieldValues[17] = value;
	}

	/**
	 * @return value of updatedAt
	 */
	public Instant getUpdatedAt(){
		return super.getTimestampValue(17);
	}
}
