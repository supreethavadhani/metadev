package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DataRow;
import java.time.Instant;
import java.time.LocalDate;
import org.simplity.fm.core.data.Schema;

/**
 * class that represents structure of schema1
 */ 
public class Schema1Data extends DataRow<Schema1> {

	/**
	 * @param schema
	 */
	public Schema1Data(final Schema1 schema) {
		super(schema);
	}

	/**
	 * set value for field1
	 * @param value to be assigned to field1
	 */
	public void setField1(long value){
		this.rawData[0] = value;
	}

	/**
	 * @return value of field1
	 */
	public long getField1(){
		return super.getLongValue(0);
	}
}
