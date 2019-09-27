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

package org.simplity.fm.core.form;

/**
 * @author simplity.org
 *
 */
public class DbMetaData {
	/**
	 * e.g. where a=? and b=?
	 */
	public String whereClause;
	/**
	 * db parameters to be used for the where clause
	 */
	public FormDbParam[] whereParams;
	/**
	 * e.g. where a=? and b=?
	 */
	public String uniqueClause;
	/**
	 * db parameters to be used for the where clause
	 */
	public FormDbParam[] uniqueParams;
	/**
	 * e.g. select a,b,c from t
	 */
	public String selectClause;
	/**
	 * db parameters to be used to receive data from the result set of the
	 * select query
	 */
	public FormDbParam[] selectParams;
	/**
	 * e.g insert a,b,c,d into table1 values(?,?,?,?)
	 */
	public String insertClause;
	/**
	 * db parameters for the insert sql
	 */
	public FormDbParam[] insertParams;

	/**
	 * e.g. update table1 set a=?, b=?, c=?
	 */
	public String updateClause;
	/**
	 * db parameters for the update sql
	 */
	public FormDbParam[] updateParams;

	/**
	 * e.g. delete from table1. Note that where is not part of this.
	 */
	public String deleteClause;

	/**
	 * db column name that is generated as internal key. null if this is not
	 * relevant
	 */
	public String generatedColumnName;
	/**
	 * meta data for the child form. null if
	 */
	public DbLink[] dbLinks;

	/**
	 * array index corresponds to DbOperation.orinal(). true if that operation
	 * is allowed
	 */
	public boolean[] dbOperationOk = new boolean[IoType.values().length];

	/**
	 * if this APP is designed for multi-tenant deployment, and this table has
	 * data across tenants..
	 */
	public Field tenantField;

	/**
	 * if this table allows update, and needs to use time-stamp-match technique
	 * to avoid concurrent updates..
	 */
	public Field timestampField;
}
