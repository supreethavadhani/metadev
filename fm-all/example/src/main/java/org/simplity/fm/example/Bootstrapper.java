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

import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.AppConfigProvider;
import org.simplity.fm.core.app.App.Config;
import org.simplity.fm.core.conf.IDbConnectionFactory;
import org.simplity.fm.core.rdb.DefaultConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * examples of providing
 *
 * @author simplity.org
 *
 */
public class Bootstrapper implements AppConfigProvider {
	private static final Logger logger = LoggerFactory.getLogger(Bootstrapper.class);

	@Override
	public Config getConfig() {
		final App.Config config = new Config();
		config.appName = "simplityExample";
		/*
		 * so long as you keep this class in the rot package, it is safe to use
		 * the following..
		 */
		config.appRootPackage = this.getClass().getPackage().getName();
		/*
		 * set all other classes here...
		 */
		// config.dbConnectionFactory = dbSetupWithConString();
		return config;
	}

	/**
	 * method to be used if this app wants to use connection string for db
	 * connection
	 *
	 * @param conString
	 * @param driverName
	 */
	@SuppressWarnings("unused")
	private static IDbConnectionFactory dbSetupWithConString() {
		final String conString = "Get it from wherever you are to get it, but never hard code in the code here";
		final String driverName = "driver name for the vendor/provider of JDBC you are using";
		logger.info("Setting up db with driver name = {} and connectionString=****", driverName);
		return DefaultConnectionFactory.getFactory(conString, driverName);
	}

	/**
	 * method to be used to set up rdbms using data source JNDI
	 *
	 * @param dataSourceName
	 */
	@SuppressWarnings("unused")
	private static IDbConnectionFactory dbSetupWithDataSource() {
		final String dataSourceName = "the designated JNDI name as per the documentation of the container/framework that provides this service";
		logger.info("Setting up db with dataSource name = {}", dataSourceName);
		return DefaultConnectionFactory.getFactory(dataSourceName);
	}

	/**
	 * method to be used if the app has a custom connection factory
	 *
	 * @param factory
	 */
	@SuppressWarnings("unused")
	private static IDbConnectionFactory dbSetupWithCustomFctory() {
		/*
		 * write the code to get the factory, whichever you are supposed to get
		 * it..
		 */
		final IDbConnectionFactory factory = null;
		// logger.info("Setting up db with a custom connection factory class
		// {}", factory.getClass().getName());
		return factory;
	}

}
