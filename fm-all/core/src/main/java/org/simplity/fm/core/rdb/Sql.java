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

package org.simplity.fm.core.rdb;

import java.io.StringWriter;

import org.simplity.fm.core.data.ValueObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public abstract class Sql {
	protected static final Logger logger = LoggerFactory.getLogger(Sql.class);
	protected String sqlText;
	protected ValueObject inputData;

	protected void setInputValue(final int idx, final Object value) {
		this.inputData.setValue(idx, value);
	}

	/**
	 * @return string that describe set the current state of this sql
	 */
	public String getState() {
		try (StringWriter writer = new StringWriter(); JsonWriter jw = new JsonWriter(writer)) {
			this.inputData.serializeFields(jw);
			return "SQL= " + this.sqlText + "\n" + writer.toString();
		} catch (final Exception e) {
			return "";
		}
	}
}
