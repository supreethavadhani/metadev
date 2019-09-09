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

import org.simplity.fm.core.datatypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * static class that locates a data type instance
 * @author simplity.org
 *
 */
public abstract class DataTypes {
	protected static final Logger logger = LoggerFactory.getLogger(DataTypes.class);
	private static final IDataTypes instance = locateTypes();
	
	/**
	 * 
	 * @return an instance of 
	 */
	public static IDataTypes getInstance() {
		return instance;
	}
	
	private static IDataTypes locateTypes() {
		Config config = Config.getConfig();
		String cls = config.getGeneratedPackageName() + '.' + Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		try {
			return (IDataTypes)Class.forName(cls).newInstance();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error("Unable to locate generated data types class {}. No data types will be served for this app now.", cls);
			return new IDataTypes() {
				
				@Override
				public DataType getDataType(String name) {
					logger.error("Class {} is not located. NO data types in the repository.", cls);
					return null;
				}
			};
		}
	}
}
