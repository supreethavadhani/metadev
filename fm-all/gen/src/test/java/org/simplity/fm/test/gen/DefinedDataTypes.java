package org.simplity.fm.test.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.IDataTypes;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.datatypes.TextType;
import org.simplity.fm.core.datatypes.IntegerType;
import org.simplity.fm.core.datatypes.DecimalType;
import org.simplity.fm.core.datatypes.BooleanType;
import org.simplity.fm.core.datatypes.DateType;
import org.simplity.fm.core.datatypes.TimestampType;

/**
 * class that has static attributes for all data types defined for this project. It also extends <code>DataTypes</code>
 */ 
public class DefinedDataTypes implements IDataTypes {
	public static final DateType date = new DateType("date", "invalidDate", 73000, 73000);
	public static final IntegerType income = new IntegerType("income", "invalidIncome", 0L, 0L);
	public static final IntegerType country = new IntegerType("country", "invalidCountry", 0L, 999L);
	public static final TextType code = new TextType("code", "invalidCode", 0, 50, null);
	public static final TextType loginId = new TextType("loginId", "invalidLoginId", 0, 50, null);
	public static final BooleanType bool = new BooleanType("bool", "invalidBool");
	public static final TextType gender = new TextType("gender", "invalidGender", 0, 10, null);
	public static final IntegerType tenantKey = new IntegerType("tenantKey", "invalidTenentKey", 0L, 0L);
	public static final IntegerType integer = new IntegerType("integer", "invalidInteger", 0L, 0L);
	public static final TextType accountStatus = new TextType("accountStatus", "invalidAccountStatus", 0, 10, null);
	public static final TextType pin = new TextType("pin", "invalidPin", 6, 6, "[1-9][0-9]{5}");
	public static final TextType json = new TextType("json", "invalidJson", 0, 10000, null);
	public static final IntegerType id = new IntegerType("id", "invalidId", 0L, 0L);
	public static final TextType state = new TextType("state", "invalidState", 0, 50, null);
	public static final TextType text = new TextType("text", "invalidText", 0, 1000, null);
	public static final TextType email = new TextType("email", "invalidEmail", 0, 1000, null);
	public static final TimestampType timestamp = new TimestampType("timestamp", "invalidTimestamp");
	public static final TextType ip = new TextType("ip", "invalidIp", 0, 1000, null);
	public static final TextType url = new TextType("url", "invalidUrl", 0, 1000, null);
	public static final TextType phone = new TextType("phone", "invalidPhone", 0, 20, null);
	public static final DecimalType grade = new DecimalType("grade", "invalidGrade", 0L, 999L, 2);
	public static final TextType govtCode = new TextType("govtCode", "invalidGovtCode", 0, 50, null);
	public static final TextType name = new TextType("name", "invalidName", 0, 50, null);
	public static final TextType userType = new TextType("userType", "invalidUserType", 0, 20, null);
	public static final TextType uniqueId = new TextType("uniqueId", "invalidUniqueId", 16, 16, "[1-9][0-9]{15}");
	public static final TextType desc = new TextType("desc", "invalidDesc", 0, 1000, null);

	public static final DataType[] allTypes = {date, income, country, code, loginId, bool, gender, tenantKey, integer, accountStatus, pin, json, id, state, text, email, timestamp, ip, url, phone, grade, govtCode, name, userType, uniqueId, desc};
	 private Map<String, DataType> typesMap;
	/**
	 * default constructor
	 */
	public DefinedDataTypes() {
		this.typesMap = new HashMap<>();
		for(DataType dt: allTypes) {
			this.typesMap.put(dt.getName(), dt);
		}
	}

@Override
	public DataType getDataType(String name) {
		return this.typesMap.get(name);
	}
}
