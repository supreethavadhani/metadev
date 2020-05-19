package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.service.IServiceContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of user
 */ 
public class UserRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("userId", 0, DefinedDataTypes.flexibleId, null, null, null, "user_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("trustId", 2, DefinedDataTypes.id, null, null, null, "trust_id", ColumnType.OptionalData), 
			new DbField("userType", 3, DefinedDataTypes.userType, "Student", null, "userType", "user_type", ColumnType.RequiredData), 
			new DbField("loginId", 4, DefinedDataTypes.loginId, null, null, null, "login_id", ColumnType.RequiredData), 
			new DbField("password", 5, DefinedDataTypes.text, null, null, null, "password", ColumnType.OptionalData), 
			new DbField("loginEnabled", 6, DefinedDataTypes.bool, "false", null, null, "login_enabled", ColumnType.RequiredData), 
			new DbField("previousLoginAt", 7, DefinedDataTypes.timestamp, null, null, null, "previous_login_at", ColumnType.OptionalData), 
			new DbField("currentLoginAt", 8, DefinedDataTypes.timestamp, null, null, null, "current_login_at", ColumnType.OptionalData), 
			new DbField("resetPasswordCount", 9, DefinedDataTypes.integer, null, null, null, "reset_password_count", ColumnType.OptionalData), 
			new DbField("resetPasswordSentAt", 10, DefinedDataTypes.timestamp, null, null, null, "reset_password_sent_at", ColumnType.OptionalData), 
			new DbField("currentLoginIp", 11, DefinedDataTypes.ip, null, null, null, "current_login_ip", ColumnType.OptionalData), 
			new DbField("previousLoginIp", 12, DefinedDataTypes.ip, null, null, null, "previous_login_ip", ColumnType.OptionalData), 
			new DbField("loginCount", 13, DefinedDataTypes.integer, null, null, null, "login_count", ColumnType.OptionalData), 
			new DbField("confirmationToken", 14, DefinedDataTypes.text, null, null, null, "confirmation_token", ColumnType.OptionalData), 
			new DbField("loginToken", 15, DefinedDataTypes.text, null, null, null, "login_token", ColumnType.OptionalData), 
			new DbField("createdAt", 16, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("createdBy", 17, DefinedDataTypes.id, null, null, null, "created_by", ColumnType.CreatedBy), 
			new DbField("updatedAt", 18, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt), 
			new DbField("updatedBy", 19, DefinedDataTypes.id, null, null, null, "updated_by", ColumnType.ModifiedBy)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("user", FIELDS, VALIDS);
	/* DB related */
	private static final  boolean[] OPS = {true, true, true, true, true, false};
	private static final String SELECT = "SELECT user_id, institute_id, trust_id, user_type, login_id, password, login_enabled, previous_login_at, current_login_at, reset_password_count, reset_password_sent_at, current_login_ip, previous_login_ip, login_count, confirmation_token, login_token, created_at, created_by, updated_at, updated_by FROM users";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
	private static final  String INSERT = "INSERT INTO users(institute_id, trust_id, user_type, login_id, password, login_enabled, previous_login_at, current_login_at, reset_password_count, reset_password_sent_at, current_login_ip, previous_login_ip, login_count, confirmation_token, login_token, created_at, created_by, updated_at, updated_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP , ?,  CURRENT_TIMESTAMP , ?)";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 19};
	private static final String WHERE = " WHERE user_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE users SET trust_id= ? , user_type= ? , login_id= ? , password= ? , login_enabled= ? , previous_login_at= ? , current_login_at= ? , reset_password_count= ? , reset_password_sent_at= ? , current_login_ip= ? , previous_login_ip= ? , login_count= ? , confirmation_token= ? , login_token= ? , updated_at= CURRENT_TIMESTAMP , updated_by= ?  WHERE user_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19, 0, 1};
	private static final String DELETE = "DELETE FROM users";

	private static final Dba DBA = new Dba(FIELDS, "users", OPS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX);

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
}
