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

package org.simplity.fm.core.conf;

import org.simplity.fm.core.UserContext;

/**
 * @author simplity.org
 *
 */
public interface ISessionCache {

	/**
	 *
	 * @param id
	 *            non-null unique id/token to the user-session
	 * @param session
	 *            non-null session
	 */
	void put(String id, UserContext session);

	/**
	 * get a copy of the session that is associated with this session. The
	 * session object may be mutable. However, the cached object is not altered
	 * when the returned object is mutated.
	 *
	 * If the modified session is to be used instead of the old tone, then it
	 * must be cached explicitly with a all to put() method;
	 *
	 * @param id
	 *            unique id/token assigned to this session.
	 * @return user-session for this id. null if the id is not valid, or the
	 *         session has expired and it is
	 *         removed from the cache.
	 */
	UserContext get(String id);

	/**
	 *
	 * @param id
	 *            unique id/token assigned to this session.
	 * @return user-session that
	 */
	UserContext remove(String id);

	/**
	 * clear all entries
	 */
	void clear();
}
