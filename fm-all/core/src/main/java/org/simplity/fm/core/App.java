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

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.rdb.Sql;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.ListService;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a concrete App
 *
 * @author simplity.org
 *
 */
public class App {

	protected static final Logger logger = LoggerFactory.getLogger(App.class);

	private static IApp emptyApp;
	private static IApp defaultApp;

	private static final Map<String, IApp> namedApps = new HashMap<>();

	/**
	 * @return default app.
	 */
	public static IApp getApp() {
		if (defaultApp != null) {
			return defaultApp;
		}

		logger.warn(
				"No app is instantiated. An empty app is returned. This app has no components, and has teh default valaues for all configuration parameters");
		return getEmptyApp();
	}

	/**
	 *
	 * @param app
	 *            non-null App to be used as the default app. Note that this app
	 *            is also added as a named app.
	 */
	public void setDefaultApp(final IApp app) {
		if (defaultApp != null) {
			logger.info("Current default app is {}. This is being replaced with {}", defaultApp.getName(),
					app.getName());
		}

		defaultApp = app;
		namedApps.put(app.getName(), app);
	}

	/**
	 *
	 * @param app
	 *            non-null App to be made available in this execution context.
	 *            It is indexed by its name.
	 */
	public void setNamedApp(final IApp app) {
		final String appName = app.getName();
		final IApp oldApp = namedApps.get(appName);
		if (oldApp != null) {
			logger.info("App {} is replaced with another one with the same name. Possibly a duplicate/redundant call");
		}
		namedApps.put(appName, app);
	}

	private static IApp getEmptyApp() {
		if (emptyApp == null) {
			emptyApp = new EmptyApp();
		}
		return emptyApp;
	}

	/**
	 *
	 * @param appName
	 *            unique id/name associated with the desired app.
	 * @param getEmptyAppInsteadOfError
	 *            if false, a run time error is thrown if the app is not set for
	 *            this execution context. if set to true, a non-null app is
	 *            guaranteed.
	 * @return named app instance set in the context. If such an app is not set,
	 *         and the second parameter is set to true, an emptyApp is returned.
	 */
	public static IApp getNamedApp(final String appName, final boolean getEmptyAppInsteadOfError) {
		final IApp app = namedApps.get(appName);
		if (app != null) {
			return app;
		}

		logger.error("No App named {}", appName);
		if (getEmptyAppInsteadOfError) {
			logger.info("An empty app is returned");
			return getEmptyApp();

		}

		final String msg = "No app named " + appName + ". Throwing error.";
		logger.error(msg);
		throw new RuntimeException(msg);

	}

	protected static class EmptyApp implements IApp {

		private static void logError() {
			logger.error("An empty app is set as default. No component is avaiable in the context.");
		}

		@Override
		public String getName() {
			return "emptyApp";
		}

		@Override
		public Form getForm(final String formId) {
			logError();
			return null;
		}

		@Override
		public Schema getSchema(final String schemaName) {
			logError();
			return null;
		}

		@Override
		public DataType getDataType(final String dataTypeId) {
			logError();
			return null;
		}

		@Override
		public IValueList getValueList(final String listId) {
			logError();
			return null;
		}

		@Override
		public IService getService(final String serviceName) {
			logError();
			return null;
		}

		@Override
		public IFunction getFunction(final String functionName) {
			logError();
			return null;
		}

		@Override
		public Message getMessage(final String messageId) {
			logError();
			return null;
		}

		@Override
		public int getMaxRowsToExtractFromDb() {
			return 1000;
		}

		@Override
		public boolean treatNullAsEmptyString() {
			return true;
		}

		@Override
		public Sql getSql(final String sqlName) {
			logError();
			return null;
		}

	}

	private static final String SCHEMA = Conventions.App.SCHEMA_CLASS_SUFIX;
	private static final String FORM = Conventions.App.FORM_CLASS_SUFIX;
	private static final String SQL = Conventions.App.SQL_CLASS_SUFIX;
	private static final char DOT = '.';

	static boolean isPackage(final String name) {
		if (Package.getPackage(name) != null) {
			return true;
		}
		logger.error("{} is not a package.", name);
		return false;
	}

	protected static String toClassName(final String name) {
		int idx = name.lastIndexOf('.');
		if (idx == -1) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		idx++;
		return name.substring(0, idx) + name.substring(idx, idx + 1).toUpperCase() + name.substring(idx + 1);
	}

	protected static class DefaultApp implements IApp {
		/**
		 * attributes loaded from config json
		 */
		private String appName;
		private int maxRowsToExtractFromDb;
		private boolean treatNullAsEmptyString;

		private final IDataTypes dataTypes;
		private final String formRoot;
		private final String schemaRoot;
		private final String listRoot;
		private final String serviceRoot;
		private final String customListRoot;
		private final String fnRoot;
		private final String sqlRoot;
		private final IMessages messages;
		private final Map<String, Form> forms = new HashMap<>();
		private final Map<String, Schema> schemas = new HashMap<>();
		private final Map<String, IValueList> lists = new HashMap<>();
		private final Map<String, IService> services = new HashMap<>();
		private final Map<String, IFunction> functions = new HashMap<>();
		private final Map<String, Sql> sqls = new HashMap<>();

		protected DefaultApp(final IDataTypes dataTypes, final IMessages messages, final String rootPackage) {
			this.dataTypes = dataTypes;
			this.messages = messages;
			final String genRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_GEN + DOT;
			this.formRoot = genRoot + Conventions.App.FOLDER_NAME_FORM + DOT;
			this.schemaRoot = genRoot + Conventions.App.FOLDER_NAME_SCHEMA + DOT;
			this.listRoot = genRoot + Conventions.App.FOLDER_NAME_LIST + DOT;
			this.customListRoot = rootPackage + Conventions.App.FOLDER_NAME_CUSTOM_LIST + DOT;
			this.serviceRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_SERVICE + DOT;
			this.fnRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_FN + DOT;
			this.sqlRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_SQL + DOT;
			/*
			 * add hard-wired services to the list
			 */
			this.services.put(Conventions.App.SERVICE_LIST, ListService.getInstance());
		}

		@Override
		public Form getForm(final String formId) {
			Form form = this.forms.get(formId);
			if (form != null) {
				return form;
			}
			final String cls = this.formRoot + toClassName(formId) + FORM;
			try {
				form = (Form) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No form named {} because we could not locate class {}", formId, cls);
				return null;
			}
			this.forms.put(formId, form);
			return form;
		}

		@Override
		public Schema getSchema(final String schemaName) {
			Schema schema = this.schemas.get(schemaName);
			if (schema != null) {
				return schema;
			}
			final String cls = this.schemaRoot + toClassName(schemaName) + SCHEMA;
			try {
				schema = (Schema) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No schema named {} because we could not locate class {}", schemaName, cls);
				return null;
			}
			this.schemas.put(schemaName, schema);
			return schema;
		}

		@Override
		public Sql getSql(final String sqlName) {
			Sql sql = this.sqls.get(sqlName);
			if (sql != null) {
				return sql;
			}
			final String cls = this.sqlRoot + toClassName(sqlName) + SQL;
			try {
				sql = (Sql) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No Sql named {} because we could not locate class {}", sqlName, cls);
				return null;
			}
			this.sqls.put(sqlName, sql);
			return sql;
		}

		@Override
		public DataType getDataType(final String dataTypeId) {
			return this.dataTypes.getDataType(dataTypeId);
		}

		@Override
		public IValueList getValueList(final String listId) {
			IValueList list = this.lists.get(listId);
			if (list != null) {
				return list;
			}
			final String clsName = toClassName(listId);
			final String cls = this.listRoot + clsName;
			try {
				list = (IValueList) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				final String cls1 = this.customListRoot + clsName;
				try {
					list = (IValueList) Class.forName(cls1).newInstance();
				} catch (final Exception e1) {
					logger.error("No list named {} because we could not locate class {} or {}", listId, cls, cls1);
					return null;
				}
			}
			this.lists.put(listId, list);
			return list;
		}

		@Override
		public Message getMessage(final String messageId) {
			return this.messages.getMessage(messageId);
		}

		@Override
		public IService getService(final String serviceId) {
			IService service = this.services.get(serviceId);
			if (service != null) {
				return service;
			}
			/*
			 * we first check for a class. this approach allows us to
			 * over-ride
			 * standard formIO services
			 */
			final String cls = this.serviceRoot + toClassName(serviceId);
			try {
				service = (IService) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				/*
				 * it is not a class. Let us see if we can generate it
				 */
				service = this.tryFormIo(serviceId);
				if (service == null) {
					logger.error("Service {} is not served by this application", serviceId);
					return null;
				}
			}
			this.services.put(serviceId, service);
			return service;
		}

		private IService tryFormIo(final String serviceName) {
			final int idx = serviceName.indexOf(Conventions.Http.SERVICE_OPER_SEPARATOR);
			if (idx <= 0) {
				logger.info("Service name {} is not of the form operation_name. Service is not generated");
				return null;
			}

			final String OperationName = toClassName(serviceName.substring(0, idx));
			IoType opern = null;
			try {
				opern = IoType.valueOf(OperationName);
			} catch (final Exception e) {
				logger.warn(
						"Service name {} is of the form operation_name, but {} is not a valid operation. No service is generated",
						serviceName, OperationName);
				return null;
			}

			String formName = serviceName.substring(idx + 1);
			logger.info("Looking to generate a service for operantion {} on {}", opern, formName);

			/*
			 * we provide flexibility for the service name to have Form or
			 * Schema
			 * suffix at the end, failing which we try the name itself as
			 * form
			 */
			if (formName.endsWith(FORM)) {
				formName = formName.substring(0, formName.length() - FORM.length());
				final Form form = this.getForm(formName);
				if (form == null) {
					logger.warn("No form named {}", formName);
					return null;
				}
				return form.getService(opern);
			}

			if (formName.endsWith(SCHEMA)) {
				formName = formName.substring(0, formName.length() - SCHEMA.length());
				final Schema schema = this.getSchema(formName);
				if (schema == null) {
					logger.warn("No Schema named {}", formName);
					return null;
				}
				return schema.getService(opern);
			}

			final Form form = this.getForm(formName);
			if (form != null) {
				return form.getService(opern);
			}

			logger.info("{} is not a form or schema and hence a service is not genrated for oepration {}", formName,
					opern);
			return null;

		}

		@Override
		public IFunction getFunction(final String functionName) {
			IFunction fn = this.functions.get(functionName);
			if (fn != null) {
				return fn;
			}
			final String cls = this.fnRoot + toClassName(functionName);
			try {
				fn = (IFunction) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No Function named {} because we could not locate class {}", functionName, cls);
				return null;
			}
			this.functions.put(functionName, fn);
			return fn;
		}

		@Override
		public int getMaxRowsToExtractFromDb() {
			return this.maxRowsToExtractFromDb;
		}

		@Override
		public boolean treatNullAsEmptyString() {
			return this.treatNullAsEmptyString;
		}

		@Override
		public String getName() {
			return this.appName;
		}
	}
}