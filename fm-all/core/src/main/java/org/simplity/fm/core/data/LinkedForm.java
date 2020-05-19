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

import java.sql.SQLException;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.service.IServiceContext;

/**
 * represents a child/linked form for a parent form
 *
 * @author simplity.org
 * @param <T>
 *            dbRecord of the linked form
 *
 */
public class LinkedForm<T extends DbRecord> {
	/**
	 * how this form is linked to its parent
	 */
	private final LinkMetaData linkMeta;

	private final Form<T> form;

	/**
	 *
	 * @param linkMeta
	 * @param form
	 */
	public LinkedForm(final LinkMetaData linkMeta, final Form<T> form) {
		this.linkMeta = linkMeta;
		this.form = form;
	}

	/**
	 * read rows for this linked form based on the parent record
	 *
	 * @param parentRec
	 *            parent record
	 * @param writer
	 *            to which data is to be serialized to
	 * @param handle
	 * @throws SQLException
	 */
	public void read(final DbRecord parentRec, final ISerializer writer, final DbHandle handle) throws SQLException {
		this.linkMeta.read(parentRec, this.form, writer, handle);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean insert(final DbRecord parentRec, final IInputObject inputObject, final DbHandle handle,
			final IServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.save(parentRec, this.form, inputObject, handle, ctx);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean update(final DbRecord parentRec, final IInputObject inputObject, final DbHandle handle,
			final IServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.save(parentRec, this.form, inputObject, handle, ctx);
	}

	/**
	 * @param parentRec
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean delete(final DbRecord parentRec, final DbHandle handle, final IServiceContext ctx)
			throws SQLException {
		this.checkUpdatability();
		return this.linkMeta.delete(parentRec, this.form, handle);
	}

	private void checkUpdatability() {
		if (this.form.hasLinks()) {
			throw new ApplicationError(
					"Auto delete operation not allowed on a form with hrand-links. It can have only one level of linked forms.");
		}
	}
}
