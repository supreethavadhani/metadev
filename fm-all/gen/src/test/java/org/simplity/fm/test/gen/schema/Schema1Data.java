package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DataRow;
import org.simplity.fm.core.data.Schema;

/**
 * class that represents structure of schema1
 */
public class Schema1Data extends DataRow {
	/** **/
	public Schema1Data(final Schema schema) {
		super(schema);
	}

	/** **/
	public Schema1Data(final Schema schema, final Object[] row) {
		super(schema, row);
	}

	/** **/
	public void setField1(final long value) {
		this.rawData[0] = value;
	}

	/** **/
	public long getField1() {
		return super.getLongValue(0);
	}
}
