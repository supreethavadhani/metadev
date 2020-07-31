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

package org.simplity.fm.core;

import java.util.HashSet;
import java.util.Set;

import org.simplity.fm.core.data.OverrideUtil;
import org.simplity.fm.core.data.OverrideUtil.Overrides;
import org.simplity.fm.core.data.RecordOverride;

/**
 * data that is to be cached for a logged-in user that is used across service
 * requests. This is a base class that the actual Apps extend to make this
 * useful
 *
 * @author simplity.org
 *
 */
public class UserContext {

	protected final String userId;
	/**
	 * if form/records are overridden for this
	 */
	protected String overrideId;
	/**
	 * name of forms that are overridden in this context
	 */
	protected Set<String> formOverrides;
	/**
	 * name of records that are overridden in this context
	 */
	protected Set<String> recordOverrides;

	/**
	 *
	 * @param userId
	 */
	public UserContext(final String userId) {
		this.userId = userId;
	}

	/**
	 *
	 * @return the ID of the user to whom this session belongs to
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * to be invoked by the extended class to cache the form/record overrides
	 */
	protected void setOverrides(final String id) {
		final Overrides overs = OverrideUtil.getOverides(id);
		if (overs == null) {
			return;
		}

		this.overrideId = id;
		this.formOverrides = new HashSet<>();
		for (final String s : overs.forms) {
			this.formOverrides.add(s);
		}

		this.recordOverrides = new HashSet<>();
		for (final String s : overs.records) {
			this.recordOverrides.add(s);
		}
	}

	/**
	 *
	 * @param recordName
	 * @return true if this form is overridden in this context
	 */
	public String getRecordOverrideId(final String recordName) {
		if (this.recordOverrides != null && this.recordOverrides.contains(recordName)) {
			return this.overrideId;
		}
		return null;
	}

	/**
	 *
	 * @param formName
	 * @return id with which this form is overridden. null if it is not
	 *         overridden.
	 */
	public String getFormOverrideId(final String formName) {
		if (this.formOverrides != null && this.formOverrides.contains(formName)) {
			return this.overrideId;
		}
		return null;
	}

	/**
	 * get the record override for this record in this context
	 *
	 * @param recordName
	 * @return record override, or null if it is not found
	 */
	public RecordOverride getRecordOverride(final String recordName) {
		return OverrideUtil.getRecord(this.overrideId, recordName);
	}
}
