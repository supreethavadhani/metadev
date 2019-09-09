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

package org.simplity.fm.core;

import java.io.InputStream;
import java.util.Properties;

import org.simplity.fm.core.rdb.DefaultConnectionFactory;
import org.simplity.fm.core.rdb.IConnectionFactory;
import org.simplity.fm.core.rdb.RdbDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * all parameters used by this app that are loaded from config file
 * 
 * @author simplity.org
 *
 */
public class Config {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private static final String RES_NAME = "fm/config.properties";
	private static final String NAME1 = "generatedPackage";
	private static final String VAL1 = "example.project.gen";
	private static final String NAME2 = "generatedSourceRoot";
	private static final String VAL2 = "c:/fm/";
	private static final String NAME3 = "xlsRootFolder";
	private static final String VAL3 = "c:/fm/xls/";
	private static final String NAME4 = "customCodePackage";
	private static final String VAL4 = "example.project.custom";
	private static final String DB_FACTORY = "dbFactoryClassName";
	private static final String DB_DATA_SOURCE = "dataSourceJndiName";
	private static final String DB_CON_STRING = "dbConnectoinString";
	private static final String DB_DRIVER_NAME = "DbDriverClassName";

	private static final Config instance = load();

	/**
	 * 
	 * @return config instance
	 */
	public static Config getConfig() {
		return instance;
	}

	private static Config load() {
		logger.info("Locating resource named {} for configuraiton parameters", RES_NAME);
		Config config = new Config();
		Properties p = new Properties();
		ClassLoader loader = Config.class.getClassLoader();
		try (InputStream stream = loader.getResourceAsStream(RES_NAME)) {
			if (stream == null) {
				logger.error("Unable to locate resource {}. Config will work with hard coded values!!!", RES_NAME);
				return config;
			}
			p.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					"Exception while loading properties from {}. \nError: {}\nConfig will work with hard coded values!!!",
					RES_NAME, e.getMessage());
		}
		config.generatedPackageName = getProperty(p, NAME1, VAL1, false);
		config.generatedSourceRoot = getProperty(p, NAME2, VAL2, true);
		config.xlsRootFolder = getProperty(p, NAME3, VAL3, true);
		config.customCodePackage = getProperty(p, NAME4, VAL4, false);
		IConnectionFactory f = getFactory(p);
		if (f == null) {
			logger.error("Data base operations will not work for this application");
		} else {
			RdbDriver.setFactory(f);
			logger.info("Data base connection factory set to {} ", f.getClass().getName());
		}

		return config;
	}

	private static String getProperty(Properties p, String name, String def, boolean isFolder) {
		String val = p.getProperty(name);
		if (val == null) {
			if (def == null) {
				logger.error("no value for property {} defined.", name);
				return null;
			}
			logger.error("no value for property {} defined. A Defualt of {} assumed", name, def);
			return def;
		}
		if (isFolder) {
			char c = val.charAt(val.length() - 1);
			if (c != '/') {
				val += '/';
			}
		}
		logger.info("{} set to {}", name, val);
		return val;
	}

	private Config() {
		//
	}

	private String generatedPackageName;
	private String generatedSourceRoot;
	private String xlsRootFolder;
	private String customCodePackage;

	/**
	 * @return package name with trailing that all generated classes belong to.
	 *         We may create sub-packages in them
	 * 
	 */
	public String getGeneratedPackageName() {
		return this.generatedPackageName;
	}

	/**
	 * @return '/'-ended root source folder where sources are generated. folders
	 *         are
	 *         appended to this by the generator based on package name
	 */
	public String getGeneratedSourceRoot() {
		return this.generatedSourceRoot;
	}

	/**
	 * @return '/' ended root folder where dataTypes.xlsx is found. forms are
	 *         stored under a sub-folder named form under this folder
	 */
	public String getXlsRootFolder() {
		return this.xlsRootFolder;
	}

	/**
	 * @return package name ending with a '.' where user-defined classes are
	 *         placed
	 */
	public String getCustomCodePackage() {
		return this.customCodePackage;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getInstance(String className, Class<T> cls) {
		try {
			Class<?> c = Class.forName(className);
			Object obj = c.newInstance();
			return (T) obj;
		} catch (Exception e) {
			String msg = e.getMessage();
			logger.error("Unable to use class name {} to get an instance of type {}. Error: {}", className,
					cls.getName(), msg);
			return null;
		}

	}

	private static IConnectionFactory getFactory(Properties p) {
		String text = p.getProperty(DB_FACTORY);
		if (text != null && text.isEmpty() == false) {
			return getInstance(text, IConnectionFactory.class);
		}

		text = p.getProperty(DB_DATA_SOURCE);
		if (text != null && text.isEmpty() == false) {
			return DefaultConnectionFactory.getFactory(text);
		}

		text = p.getProperty(DB_CON_STRING);
		String driver = p.getProperty(DB_DRIVER_NAME);

		if (text == null || text.isEmpty() || driver == null || driver.isEmpty()) {
			logger.warn("RDBMS is not set up for this project.");
			return null;
		}
		return DefaultConnectionFactory.getFactory(text, driver);
	}
}
