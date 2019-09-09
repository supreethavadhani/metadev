/*
 * Copyright (c) 2015 EXILANT Technologies Private Limited (www.exilant.com)
 * Copyright (c) 2016 simplity.org
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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.simplity.fm.core.rdb;
import org.simplity.fm.core.Conventions;
/**
 *
 * @author simplity.org
 *
 */
public enum FilterCondition {
	/** equal */
	Equal(Conventions.Comparator.EQ),
	/** not equal */
	NotEqual(Conventions.Comparator.NE),
	/** greater. remember it is greater and not "more" */
	Greater(Conventions.Comparator.GT),
	/** greater or equal */
	GreaterOrEqual(Conventions.Comparator.GE),
	/**
	 * we prefer to call small rather than less because we say greater and not
	 * more :-)
	 */
	Smaller(Conventions.Comparator.LT),
	/** we prefer to smaller to less than more :-) */
	SmallerOrEqual(Conventions.Comparator.LE),
	/** like */
	Contains(Conventions.Comparator.CONTAINS),
	/** starts with */
	StartsWith(Conventions.Comparator.STARTS_WITH),
	/** between */
	Between(Conventions.Comparator.BETWEEN),
	/** one in the list */
	In(Conventions.Comparator.IN_LIST);
	private String textValue;

	private FilterCondition(String text) {
		this.textValue = text;
	}

	/**
	 * parse a text into enum
	 *
	 * @param text
	 *            text to be parsed into enum
	 * @return filter condition, or null if there is no filter for this text
	 */
	public static FilterCondition parse(String text) {
		if (text == null || text.isEmpty() || text.equals(Equal.textValue)) {
			return Equal;
		}
		for (FilterCondition f : FilterCondition.values()) {
			if (f.textValue.equals(text)) {
				return f;
			}
		}
		return null;
	}
}
