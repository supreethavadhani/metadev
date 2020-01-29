/*
 * Copyright (c) 2019 simplity.org
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

package org.simplity.fm.core;

import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.rdb.Sql;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.validn.IValueList;

/**
 * Represents an application. Configuration details are loaded at boot time.
 * Other components are located and loaded at run time on a need basis.
 *
 * @author simplity.org
 *
 */
public interface IApp {
	/**
	 * @return max number of rows to be returned by by filter service. 0 implies
	 *         no such limit to be set.
	 */
	int getMaxRowsToExtractFromDb();

	/**
	 * @return Simplity recommends using empty string instead of null in db
	 *         columns that are optional VARCHARS.
	 */
	boolean treatNullAsEmptyString();

	/**
	 *
	 * @return unique name assigned to this app.
	 */
	String getName();

	/**
	 *
	 * @param formId
	 * @return form instance, or null if such a form is not located
	 */
	Form getForm(String formId);

	/**
	 *
	 * @param schemaName
	 * @return form instance, or null if such a form is not located
	 */
	Schema getSchema(String schemaName);

	/**
	 *
	 * @param dataTypeId
	 * @return a data type instance, or null if it is not located.
	 */
	DataType getDataType(String dataTypeId);

	/**
	 *
	 * @param listId
	 * @return an instance for this id, or null if is not located
	 */
	IValueList getValueList(String listId);

	/**
	 *
	 * @param serviceName
	 * @return an instance for this id, or null if is not located
	 */
	IService getService(String serviceName);

	/**
	 *
	 * @param functionName
	 * @return an instance for this id, or null if is not located
	 */
	IFunction getFunction(String functionName);

	/**
	 *
	 * @param messageId
	 * @return message or null if no such message is located.
	 */
	Message getMessage(String messageId);

	/**
	 *
	 * @param sqlName
	 * @return sql, or null if no such sql
	 */
	Sql getSql(String sqlName);
}
