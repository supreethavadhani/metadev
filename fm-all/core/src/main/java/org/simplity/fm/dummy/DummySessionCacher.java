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

package org.simplity.fm.dummy;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.ISessionCache;
import org.simplity.fm.core.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a stand-in cacher that is nothing but a map.
 *
 * @author simplity.org
 *
 */
public class DummySessionCacher implements ISessionCache {
	private static final Logger logger = LoggerFactory.getLogger(DummySessionCacher.class);
	private final Map<String, UserSession> sessions = new HashMap<>();

	@Override
	public void put(final String id, final UserSession session) {
		if (id == null) {
			logger.error("Caching not possible for a null key.");
		} else if (session == null) {
			logger.error("Null sessions are not cachedy.");
		} else {
			this.sessions.put(id, session);
		}
	}

	@Override
	public UserSession get(final String id) {
		if (id == null) {
			logger.error("key is to be non-null for a get().");
			return null;
		}
		return this.sessions.get(id);
	}

	@Override
	public UserSession remove(final String id) {
		if (id == null) {
			logger.error("key is to be non-null for a remove().");
			return null;
		}
		return this.sessions.remove(id);
	}

	@Override
	public void clear() {
		logger.info("Sessions cleared");
		this.sessions.clear();
	}

}
