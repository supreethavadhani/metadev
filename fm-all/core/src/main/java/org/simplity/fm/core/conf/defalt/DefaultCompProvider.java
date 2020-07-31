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

package org.simplity.fm.core.conf.defalt;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.conf.ICompProvider;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValueList;

/**
 * @author simplity.org
 *
 */
public class DefaultCompProvider implements ICompProvider {
	private static void error() {
		throw new ApplicationError("The app is not configured to provide components.");
	}

	@Override
	public Form<?> getForm(final String formId) {
		error();
		return null;
	}

	@Override
	public DataType getDataType(final String dataTypeId) {
		error();
		return null;
	}

	@Override
	public IValueList getValueList(final String listId) {
		error();
		return null;
	}

	@Override
	public IService getService(final String serviceName, final IServiceContext ctx) {
		error();
		return null;
	}

	@Override
	public IFunction getFunction(final String functionName) {
		error();
		return null;
	}

	@Override
	public Message getMessage(final String messageId) {
		error();
		return null;
	}

	@Override
	public Record getRecord(final String recordName, final IServiceContext ctx) {
		error();
		return null;
	}

	@Override
	public Record getRecord(final String recordName) {
		error();
		return null;
	}

	@Override
	public Form<?> getForm(final String formId, final IServiceContext ctx) {
		error();
		return null;
	}
}
