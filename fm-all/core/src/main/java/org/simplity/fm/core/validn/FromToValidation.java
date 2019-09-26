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

package org.simplity.fm.core.validn;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.form.FormData;

/**
 * pair of fields that form a range of values
 * 
 * @author simplity.org
 *
 */
public class FromToValidation implements IValidation {
	private final String fieldName;
	private final int fromIndex;
	private final int toIndex;
	private final boolean equalOk;
	private final String messageId;

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @param equalOk
	 * @param fieldName 
	 * @param messageId
	 */
	public FromToValidation(int fromIndex, int toIndex, boolean equalOk, String fieldName, String messageId) {
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.equalOk = equalOk;
		this.fieldName = fieldName;
		this.messageId = messageId;
	}

	@Override
	public boolean isValid(FormData formData, List<Message> messages) {
		Object fm = formData.getObject(this.fromIndex);
		Object to = formData.getObject(this.toIndex);
		if(fm == null || to == null) {
			return true;
		}
		
		boolean ok = false;
		if(fm instanceof Long) {
			ok = this.longOk((long) fm, (long)to);
		}else if(fm instanceof LocalDate) {
			ok = this.dateOk((LocalDate)fm, (LocalDate)to);
		}else if(fm instanceof Double) {
			ok = this.doubleOk((double)fm, (double)to);
		}else if(fm instanceof Instant) {
			ok = this.timestampOk((Instant)fm, (Instant)to);
		}else {
			ok = this.textOk(fm.toString(), to.toString());
		}
		if(ok) {
			return true;
		}

		messages.add(Message.newFieldError(this.fieldName, this.messageId));
		return false;
	}

	/**
	 * @param fm
	 * @param to
	 * @return
	 */
	private boolean timestampOk(Instant fm, Instant to) {
		if (this.equalOk) {
			return !fm.isAfter(to);
		}
		return to.isAfter(fm);
	}

	private boolean longOk(long fm, long to) {
		if (this.equalOk) {
			return to >= fm;
		}
		return to > fm;
	}

	private boolean doubleOk(double fm, double to) {
		if (this.equalOk) {
			return to >= fm;
		}
		return to > fm;
	}

	private boolean dateOk(LocalDate fm, LocalDate to) {
		if (this.equalOk) {
			return !fm.isAfter(to);
		}
		return to.isAfter(fm);
	}

	private boolean textOk(String fm, String to) {
		int n = to.compareToIgnoreCase(fm);
		if (this.equalOk) {
			return n >= 0;
		}
		return n > 0;
	}


	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}
