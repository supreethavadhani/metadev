package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaData;
import java.time.Instant;
import java.time.LocalDate;

/**
 * class that represents structure of trustee
 */ 
public class TrusteeData extends SchemaData {

	protected TrusteeData(final Trustee schema, final Object[] data) {
		super(schema, data);
	}

	/**
	 * set value for trusteeId
	 * @param value to be assigned to trusteeId
	 */
	public void setTrusteeId(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of trusteeId
	 */
	public long getTrusteeId(){
		return super.getLongValue(0);
	}

	/**
	 * set value for trustId
	 * @param value to be assigned to trustId
	 */
	public void setTrustId(long value){
		this.fieldValues[1] = value;
	}

	/**
	 * @return value of trustId
	 */
	public long getTrustId(){
		return super.getLongValue(1);
	}

	/**
	 * set value for name
	 * @param value to be assigned to name
	 */
	public void setName(String value){
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName(){
		return super.getStringValue(2);
	}

	/**
	 * set value for phoneNumber
	 * @param value to be assigned to phoneNumber
	 */
	public void setPhoneNumber(String value){
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of phoneNumber
	 */
	public String getPhoneNumber(){
		return super.getStringValue(3);
	}

	/**
	 * set value for email
	 * @param value to be assigned to email
	 */
	public void setEmail(String value){
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of email
	 */
	public String getEmail(){
		return super.getStringValue(4);
	}

	/**
	 * set value for designation
	 * @param value to be assigned to designation
	 */
	public void setDesignation(String value){
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of designation
	 */
	public String getDesignation(){
		return super.getStringValue(5);
	}

	/**
	 * set value for createdAt
	 * @param value to be assigned to createdAt
	 */
	public void setCreatedAt(Instant value){
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of createdAt
	 */
	public Instant getCreatedAt(){
		return super.getTimestampValue(6);
	}

	/**
	 * set value for updatedAt
	 * @param value to be assigned to updatedAt
	 */
	public void setUpdatedAt(Instant value){
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of updatedAt
	 */
	public Instant getUpdatedAt(){
		return super.getTimestampValue(7);
	}
}
