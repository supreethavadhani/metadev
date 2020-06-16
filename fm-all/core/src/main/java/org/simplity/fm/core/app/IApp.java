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

package org.simplity.fm.core.app;

import org.simplity.fm.core.conf.IAccessController;
import org.simplity.fm.core.conf.ICompProvider;
import org.simplity.fm.core.conf.IExceptionListener;
import org.simplity.fm.core.conf.IRequestLogger;
import org.simplity.fm.core.conf.IServiceContextFactory;
import org.simplity.fm.core.conf.ISessionCache;
import org.simplity.fm.core.conf.ITexter;
import org.simplity.fm.core.rdb.RdbDriver;

/**
 * Represents an application. Configuration details are loaded at boot time.
 * Other components are located and loaded at run time on a need basis.
 *
 * @author simplity.org
 *
 */
public interface IApp {
	/**
	 * @return max number of rows to be returned by by filter service. 0 implies
	 *         no such limit to be set.
	 */
	int getMaxRowsToExtractFromDb();

	/**
	 * @return Simplity recommends using empty string instead of null in db
	 *         columns that are optional VARCHARS.
	 */
	boolean treatNullAsEmptyString();

	/**
	 *
	 * @return non-null unique name assigned to this app.
	 */
	String getName();

	/**
	 * get component provider
	 *
	 * @return non-null component provider. throws ApplicationError if component
	 *         provider is not set-up for this app.
	 */
	ICompProvider getCompProvider();

	/**
	 *
	 * @return non-null driver. throws ApplicationError if DBDriver is not set
	 *         for this app.
	 */
	RdbDriver getDbDriver();

	/**
	 *
	 * @return the access controllers
	 */
	IAccessController getAccessController();

	/**
	 *
	 * @return the exception listener
	 */
	IExceptionListener getExceptionListener();

	/**
	 *
	 * @return the session cache
	 */
	ISessionCache getSessionCache();

	/**
	 *
	 * @return the request logger
	 */
	IRequestLogger getRequestLogger();

	/**
	 *
	 * @return the utility to send text messages to mobile phones
	 */
	ITexter getTexter();

	/**
	 *
	 * @return the factory that creates a service context for the service to be
	 *         called based on the inputs available
	 */
	IServiceContextFactory getContextFactory();
}
