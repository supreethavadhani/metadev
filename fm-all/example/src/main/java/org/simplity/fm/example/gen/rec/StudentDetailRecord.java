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
 * class that represents structure of studentDetail
 */ 
public class StudentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentId", 0, DefinedDataTypes.flexibleId, "-1", null, null, "student_id", ColumnType.PrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("departmentId", 2, DefinedDataTypes.id, null, null, "departmentList", "department_id", ColumnType.RequiredData), 
			new DbField("departmentName", 3, DefinedDataTypes.text, null, null, null, "department_name", ColumnType.RequiredData), 
			new DbField("usn", 4, DefinedDataTypes.text, null, null, null, "usn", ColumnType.OptionalData), 
			new DbField("name", 5, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("phoneNumber", 6, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("studentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final  boolean[] OPS = {true, true, true, true, true, false};
	private static final String SELECT = "SELECT student_id, institute_id, department_id, department_name, usn, name, phone_number FROM student_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6};
	private static final  String INSERT = "INSERT INTO student_details(student_id, institute_id, department_id, department_name, usn, name, phone_number) values (?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6};
	private static final String WHERE = " WHERE student_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE student_details SET department_id= ? , department_name= ? , usn= ? , name= ? , phone_number= ?  WHERE student_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 0, 1};
	private static final String DELETE = "DELETE FROM student_details";

	private static final Dba DBA = new Dba(FIELDS, "student_details", OPS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX);

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
}
