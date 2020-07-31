/*
 * Copyright (c) 2020 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.data;

import java.util.Map;

/**
 * tenant specific over-rides for a record. This feature provides flexibility to
 * change basic field-level validations by tenant. For example, we may have all
 * possible fields across all tenants in the customer record. specific tenants
 * can choose to redefine one or more fields in this.
 *
 * @author simplity.org
 *
 */
public class RecordOverride {
	/**
	 * name of the record.
	 */
	String name;
	/**
	 * tenant id for which this override is meant for
	 */
	String tenantId;
	/**
	 * field definitions to be overridden. note that the field name must exist
	 * in the record, and this can only change its meta-data
	 */
	Map<String, FieldOverride> fields;
}
