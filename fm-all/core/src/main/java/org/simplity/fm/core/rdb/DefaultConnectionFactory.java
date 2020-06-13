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

package org.simplity.fm.core.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.simplity.fm.core.IDbConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * serves as an example, or even a base class, for an application to design its
 * IConnecitonFactory class.
 * 
 * @author simplity.org
 *
 */
public class DefaultConnectionFactory implements IDbConnectionFactory {
	private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionFactory.class);
	
	/**
	 * get a factory that gets connection to default schema. This factory can not get connection to any other schema
	 * @param conString non-null connection string
	 * @param driverClassName non-null driver class name
	 * @return factory that can be used to get connection to a default schema. null in case the credentials could not be used to get a sample connection
	 */
	public static IDbConnectionFactory getFactory(String conString, String driverClassName) {
		IFactory f = getCsFactory(conString, driverClassName);
		if(f == null) {
			return null;
		}
		return new DefaultConnectionFactory(f, null, null);
	}
	
	/**
	 * get a factory that gets connection to default schema. This factory an not get connection to any other schema
	 * @param dataSourceName non-null jndi name for data source
	 * @return factory that can be used to get connection to a default schema. null in case the credentials could not be used to get a sample connection
	 */
	public static IDbConnectionFactory getFactory(String dataSourceName) {
		IFactory f = getDsFactory(dataSourceName);
		if(f == null) {
			return null;
		}
		return new DefaultConnectionFactory(f, null, null);
	}
	
	private final IFactory defFactory;
	private final IFactory altFactory;
	private final String altSchema;
	
	private  DefaultConnectionFactory(IFactory defFactory, String altSchema, IFactory altFactory) {
		this.defFactory = defFactory;
		this.altFactory = altFactory;
		this.altSchema = altSchema;
	}
	@Override
	public Connection getConnection() throws SQLException {
		if(this.defFactory == null) {
			throw new SQLException("No credentials set up for accessing a db");
		}
		return this.defFactory.getConnection();
	}

	@Override
	public Connection getConnection(String schemaName) throws SQLException {
		if(this.altSchema == null || this.altFactory == null || this.altSchema.contentEquals(schemaName) == false) {
				throw new SQLException("No credentials set up for schema {}", schemaName);
		}
		return this.altFactory.getConnection();
	}

	private static IFactory getDsFactory(String jndiName) {
		try {
			DataSource ds = (DataSource) new InitialContext().lookup(jndiName);
			/*
			 * test it..
			 */
			IFactory factory = new DsBasedFactory(ds);
			// let us test it..
			factory.getConnection().close();
			logger.info("DB driver set successfully based on JNDI name {} ", jndiName);
			return factory;
		} catch (Exception e) {
			logger.error("Error while using {} as data source. {} ", jndiName, e.getMessage());
			return null;
		}

	}
	private static IFactory getCsFactory(String conString, String driverClassName) {
		try {
			Class.forName(driverClassName);
			IFactory factory = new CsBasedFactory(conString);
			/*
			 * test it
			 */
			factory.getConnection().close();
			logger.info("DB driver set based on connection string for driver {} ", driverClassName);
			return factory;
		} catch (Exception e) {
			logger.error("Error while using a connection striing as data source. {} ", e.getMessage());
			return null;
		}
	}

	protected interface IFactory {
		Connection getConnection() throws SQLException;
	}

	protected static class DsBasedFactory implements IFactory {
		private final DataSource ds;

		protected DsBasedFactory(DataSource ds) {
			this.ds = ds;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return this.ds.getConnection();
		}

	}

	protected static class CsBasedFactory implements IFactory {
		private final String conString;

		protected CsBasedFactory(String conString) {
			this.conString = conString;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(this.conString);
		}

	}
}
