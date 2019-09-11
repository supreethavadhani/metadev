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

package org.simplity.fm.example;

import org.simplity.fm.core.rdb.DefaultConnectionFactory;
import org.simplity.fm.core.rdb.IConnectionFactory;
import org.simplity.fm.core.rdb.RdbDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Bootstrapper {
	private static final Logger logger = LoggerFactory.getLogger(Bootstrapper.class);
	/*
	 * these have to be taken from a config file in production..
	 */
	//private static final String CON_STRING = "";
	//private static final String DRIVER_NAME = "";
	//private static final String DB_FACTORY = "dbFactoryClassName";
	//private static final String DB_DATA_SOURCE = "dataSourceJndiName";
	
	/**
	 * MUST be called before running the APP, even for testing
	 */
	public static void bootstrap() {
		/*
		 * db setup
		 */
		noDbSetup();
		
	}
	/**
	 * method to be used if this app wants to use connection string for db connection
	 * @param conString
	 * @param driverName
	 */
	@SuppressWarnings("unused")
	private static void dbSetupWithConString(String conString, String driverName) {
		logger.info("Setting up db with driver name = {} and connectionString=****", driverName);
		IConnectionFactory factory = DefaultConnectionFactory.getFactory(conString, driverName);
		RdbDriver.setFactory(factory);
	}

	/**
	 * method to be used to set up rdbms using data source JNDI
	 * @param dataSourceName
	 */
	@SuppressWarnings("unused")
	private static void dbSetupWithDataSource(String dataSourceName) {
		logger.info("Setting up db with dataSource name = {}", dataSourceName);
		IConnectionFactory factory = DefaultConnectionFactory.getFactory(dataSourceName);
		RdbDriver.setFactory(factory);
	}

	/**
	 * method to be used if the app has a custom connection factory
	 * @param factory
	 */
	@SuppressWarnings("unused")
	private static void dbSetupWithCustomFctory(IConnectionFactory factory) {
		logger.info("Setting up db with a custom connection factory class {}", factory.getClass().getName());
		RdbDriver.setFactory(factory);
	}
	
	/**
	 * if the app does not need any db connection - typically for some testing purposes
	 */
	private static void noDbSetup() {
		logger.info("App has decided to live with no access to any rdbms. Any call to RdbDriver() will result in SqlException being thrown"); 
	}
}
