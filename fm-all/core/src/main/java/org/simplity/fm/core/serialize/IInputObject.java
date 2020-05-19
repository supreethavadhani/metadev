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

package org.simplity.fm.core.serialize;

/**
 * This is a common API for de-serializers like JSONObject or XML. This goes
 * hand-in-hand with <code>IInputArray</code>
 * APIs are designed for reading pre-determined element, and not for exploring
 * what it contains. For example, there is no concept of abstract-element. That
 * is, the client-program should know what it is expecting a member to be. Of
 * course, it limits its usage, but that is by design.
 *
 * @author simplity.org
 *
 */
public interface IInputObject {

	/**
	 *
	 * @return an empty object. This can be useful in avoiding nulls when
	 *         optional objects are expected from the input
	 */
	IInputObject getEmptyObject();

	/**
	 *
	 * @return an empty array. This can be useful in avoiding nulls when
	 *         optional array members are expected from the input
	 */
	IInputArray getEmptyArray();

	/**
	 *
	 * @param name
	 * @return null if no member with that name, or if the member is not an
	 *         <code>IInputObject</code>
	 */
	IInputObject getObject(String name);

	/**
	 *
	 * @param name
	 * @return null if no such member, or the value is not an array.
	 */
	IInputArray getArray(String name);

	/**
	 * value is zero if the member is missing or is not a number. getText() may
	 * be used if there is a need to differentiate zero from missing member
	 *
	 * @param name
	 * @return 0 if member is non-text, non-numeric. text is parsed into
	 *         integral value
	 */
	long getLong(String name);

	/**
	 *
	 * @param name
	 * @return null if member is missing, or is not a primitive. it is null if
	 *         the member is IInputObject or IInputArray.
	 */
	String getString(String name);

	/**
	 *
	 * @param name
	 * @return true if the member is boolean and is true. Also true if it is
	 *         text 'true', or '1' or integer 1; false otherwise
	 */
	boolean getBoolean(String name);

	/**
	 *
	 * @param name
	 * @return if member is text, it is parsed into decimal. 0 if it is non-text
	 *         and non-number
	 */
	double getDecimal(String name);

	/**
	 *
	 * @return true if the object has no members
	 */
	boolean isEmpty();

	/**
	 *
	 * @return member names
	 */
	Iterable<String> names();
}
