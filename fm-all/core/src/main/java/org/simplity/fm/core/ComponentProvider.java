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
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.ListService;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses ServiceLoader() to look for a provider, if a provider is not explicitly
 * set before any request is made.
 * Uses a default empty provider instead of throwing exception. That is, if no
 * provider is available, all requests will responded with null, after logging
 * an error message.
 *
 * @author simplity.org
 *
 */
public abstract class ComponentProvider {
	/**
	 *
	 * @param formId
	 * @return form instance, or null if such a form is not located
	 */
	public abstract Form getForm(String formId);

	/**
	 *
	 * @param schemaName
	 * @return form instance, or null if such a form is not located
	 */
	public abstract Schema getSchema(String schemaName);

	/**
	 *
	 * @param dataTypeId
	 * @return a data type instance, or null if it is not located.
	 */
	public abstract DataType getDataType(String dataTypeId);

	/**
	 *
	 * @param listId
	 * @return an instance for this id, or null if is not located
	 */
	public abstract IValueList getValueList(String listId);

	/**
	 *
	 * @param serviceName
	 * @return an instance for this id, or null if is not located
	 */
	public abstract IService getService(String serviceName);

	/**
	 *
	 * @param functionName
	 * @return an instance for this id, or null if is not located
	 */
	public abstract IFunction getFunction(String functionName);

	/**
	 *
	 * @param messageId
	 * @return message or null if no such message is located.
	 */
	public abstract Message getMessage(String messageId);

	protected static final String ERROR = "Unable to locate IComponentProvider. No components areavailable for this run.";
	private static final char DOT = '.';
	protected static final Logger logger = LoggerFactory.getLogger(ComponentProvider.class);

	private static ComponentProvider instance = null;

	/**
	 *
	 * @return non-null component provider. A default provider if no provider is
	 *         located on the class-path. this default provider will return null
	 *         for all requests, after reporting an error, but will not throw
	 *         any exceptions
	 */
	public static ComponentProvider getProvider() {
		if (instance != null) {
			return instance;
		}

		final long startedAt = System.currentTimeMillis();
		final Iterator<IPackageNameProvider> iter = ServiceLoader.load(IPackageNameProvider.class).iterator();
		if (iter.hasNext()) {
			final IPackageNameProvider p = iter.next();
			final String root = p.getCompRootPackageName();
			instance = newInstance(root);
			if (instance != null) {
				logger.info("{} located as IComponentProvider in {} ms.", instance.getClass().getName(),
						(System.currentTimeMillis() - startedAt));
				if (iter.hasNext()) {
					logger.warn(
							"Found {} as an additional IComponentProvider. Ignoring this as well as any more possible matches.",
							iter.next().getClass().getName());
				}
				return instance;
			}
		}
		logger.error(ERROR);
		instance = getStandinProvider();
		return instance;
	}

	/**
	 * @param generatedRootPackageName
	 * @return
	 */
	private static ComponentProvider newInstance(final String rootPackageName) {

		final String genRoot = rootPackageName + DOT + Conventions.App.FOLDER_NAME_GEN + DOT;
		String cls = genRoot + Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		IDataTypes types = null;
		try {
			types = (IDataTypes) Class.forName(cls).newInstance();
		} catch (final Exception e) {
			logger.error("Unable to locate class {}  as IDataTypes", cls);
			return null;
		}

		IMessages messages = null;
		try {
			cls = genRoot + Conventions.App.GENERATED_MESSAGES_CLASS_NAME;
			messages = (IMessages) Class.forName(cls).newInstance();
		} catch (final Exception e) {
			logger.warn("Unable to locate class {}  as IMessages. YOu will see only message ids, and not message texts",
					cls);
		}

		return new CompProvider(types, messages, rootPackageName);
	}

	/**
	 *
	 * @return a provider who responds with null for all requests.
	 */
	private static ComponentProvider getStandinProvider() {
		return new ComponentProvider() {

			@Override
			public IValueList getValueList(final String listId) {
				return null;
			}

			@Override
			public Form getForm(final String formId) {
				return null;
			}

			@Override
			public DataType getDataType(final String dataTypeId) {
				return null;
			}

			@Override
			public Message getMessage(final String messageId) {
				return null;
			}

			@Override
			public IService getService(final String serviceName) {
				return null;
			}

			@Override
			public IFunction getFunction(final String functionName) {
				return null;
			}

			@Override
			public Schema getSchema(final String schemaName) {
				return null;
			}
		};
	}

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

	private static class CompProvider extends ComponentProvider {
		private final IDataTypes dataTypes;
		private final String formRoot;
		private final String schemaRoot;
		private final String listRoot;
		private final String serviceRoot;
		private final String fnRoot;
		private final IMessages messages;
		private final Map<String, Form> forms = new HashMap<>();
		private final Map<String, Schema> schemas = new HashMap<>();
		private final Map<String, IValueList> lists = new HashMap<>();
		private final Map<String, IService> services = new HashMap<>();
		private final Map<String, IFunction> functions = new HashMap<>();

		protected CompProvider(final IDataTypes dataTypes, final IMessages messages, final String rootPackage) {
			this.dataTypes = dataTypes;
			this.messages = messages;
			final String genRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_GEN + DOT;
			this.formRoot = genRoot + Conventions.App.FOLDER_NAME_FORM + DOT;
			this.schemaRoot = genRoot + Conventions.App.FOLDER_NAME_SCHEMA + DOT;
			this.listRoot = genRoot + Conventions.App.FOLDER_NAME_LIST + DOT;
			this.serviceRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_SERVICE + DOT;
			this.fnRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_FN + DOT;
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
			final String cls = this.formRoot + toClassName(formId);
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
			final String cls = this.schemaRoot + toClassName(schemaName);
			try {
				schema = (Schema) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No form named {} because we could not locate class {}", schemaName, cls);
				return null;
			}
			this.schemas.put(schemaName, schema);
			return schema;
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
			final String cls = this.listRoot + toClassName(listId);
			try {
				list = (IValueList) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.error("No list named {} because we could not locate class {}", listId, cls);
				return null;
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
			 * we first check for a class. this approach allows us to over-ride
			 * standard formIO services
			 */
			final String cls = this.serviceRoot + toClassName(serviceId);
			try {
				service = (IService) Class.forName(cls).newInstance();
			} catch (final Exception e) {
				logger.info("Service {} is not defined as a java class {}", serviceId, cls);

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
				return null;
			}

			final String formName = serviceName.substring(idx + 1);
			final Form form = this.getForm(formName);
			if (form == null) {
				return null;
			}

			IoType opern = null;
			try {
				opern = IoType.valueOf(serviceName.substring(0, idx).toUpperCase());
			} catch (final Exception e) {
				return null;
			}

			return form.getService(opern);
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

	}
}
