package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.SchemaData;
import java.time.Instant;
import java.time.LocalDate;

/**
 * class that represents structure of schema1
 */ 
public class Schema1Data extends SchemaData {

	protected Schema1Data(final Schema1 schema, final Object[] data) {
		super(schema, data);
	}

	/**
	 * set value for field1
	 * @param value to be assigned to field1
	 */
	public void setField1(long value){
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of field1
	 */
	public long getField1(){
		return super.getLongValue(0);
	}
}
