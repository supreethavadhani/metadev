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

package org.simplity.fm.core.data;

import org.simplity.fm.core.service.FormBulkUpdater;
import org.simplity.fm.core.service.FormDeleter;
import org.simplity.fm.core.service.FormFilterer;
import org.simplity.fm.core.service.FormInserter;
import org.simplity.fm.core.service.FormReader;
import org.simplity.fm.core.service.FormUpdater;
import org.simplity.fm.core.service.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a schema for input from a client or output to a client
 *
 * @author simplity.org
 *
 */
public class Form {
	static protected final Logger logger = LoggerFactory.getLogger(Form.class);

	protected final String name;
	protected final Schema schema;
	protected final LinkedForm[] linkedForms;

	/**
	 *
	 * @param name
	 * @param schema
	 * @param linkedForms
	 */
	public Form(final String name, final Schema schema, final LinkedForm[] linkedForms) {
		this.schema = schema;
		this.linkedForms = linkedForms;
		this.name = name;
	}

	/**
	 * get the service instance for the desired operation on this form
	 *
	 * @param opern
	 * @return service, or null if this form is not designed for this opetatio
	 */
	public IService getService(final IoType opern) {
		final DbMetaData meta = this.schema.getDbMetaData();

		if (meta == null || meta.dbOperationOk[opern.ordinal()] == false) {
			return null;
		}

		switch (opern) {
		case CREATE:
			return new FormInserter(this);

		case DELETE:
			return new FormDeleter(this);

		case FILTER:
			return new FormFilterer(this);

		case GET:
			return new FormReader(this);

		case UPDATE:
			return new FormUpdater(this);

		case BULK:
			return new FormBulkUpdater(this);

		default:
			logger.error("Form operation {} not yet implemented", opern);
			return null;
		}

	}

	/**
	 *
	 * @return unique name of this form
	 */
	public String getFormId() {
		return this.name;
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

}
