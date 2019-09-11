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

import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.form.Form;
import org.simplity.fm.core.form.FormIo;
import org.simplity.fm.core.form.IoType;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses ServiceLoader() to look for a provider, if a provider is not explicitly
 * set before any request is made.
 * Uses a default empty provider instead of throwing exception. That is, if no
 * provider is available, all requests will responded with null, after logging
 * an error mesage.
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

		long startedAt = System.currentTimeMillis();
		Iterator<IPackageNameProvider> iter = ServiceLoader.load(IPackageNameProvider.class).iterator();
		if (iter.hasNext()) {
			IPackageNameProvider p = iter.next();
			String gen = p.getGeneratedRootPackageName();
			String ser = p.getServiceRootPackageName();
			instance = newInstance(gen, ser);
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
	private static ComponentProvider newInstance(String rootPackageName, String serviceRootName) {

		String cls = rootPackageName + DOT + Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;
		IDataTypes types = null;
		IMessages messages = null;
		try {
			types = (IDataTypes) Class.forName(cls).newInstance();
		} catch (Exception e) {
			logger.error("Unable to locate class {}  as IDataTypes", cls);
			return null;
		}

		try {
			cls = rootPackageName + DOT + Conventions.App.GENERATED_MESSAGES_CLASS_NAME;
			messages = (IMessages) Class.forName(cls).newInstance();
		} catch (Exception e) {
			logger.error("Unable to locate class {}  as IMessages", cls);
		}

		return new CompProvider(types, messages, rootPackageName, serviceRootName);
	}

	/**
	 * 
	 * @return a provider who responds with null for all requests.
	 */
	private static ComponentProvider getStandinProvider() {
		return new ComponentProvider() {

			@Override
			public IValueList getValueList(String listId) {
				return null;
			}

			@Override
			public Form getForm(String formId) {
				return null;
			}

			@Override
			public DataType getDataType(String dataTypeId) {
				return null;
			}

			@Override
			public Message getMessage(String messageId) {
				return null;
			}

			@Override
			public IService getService(String serviceName) {
				return null;
			}
		};
	}

	static boolean isPackage(String name) {
		if (Package.getPackage(name) != null) {
			return true;
		}
		logger.error("{} is not a package.", name);
		return false;
	}

	protected static String toClassName(String name) {
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
		private final String listRoot;
		private final String serviceRoot;
		private final IMessages messages;
		private final Map<String, Form> forms = new HashMap<>();
		private final Map<String, IValueList> lists = new HashMap<>();
		private final Map<String, IService> services = new HashMap<>();

		protected CompProvider(IDataTypes dataTypes, IMessages messages, String genRootName, String serviceRootName) {
			this.dataTypes = dataTypes;
			this.messages = messages;
			this.formRoot = genRootName + DOT + Conventions.App.FOLDER_NAME_FORM + DOT;
			this.listRoot = genRootName + DOT + Conventions.App.FOLDER_NAME_LIST + DOT;
			this.serviceRoot = serviceRootName + DOT;
		}

		@Override
		public Form getForm(String formId) {
			Form form = this.forms.get(formId);
			if (form != null) {
				return form;
			}
			String cls = this.formRoot + toClassName(formId);
			try {
				form = (Form) Class.forName(cls).newInstance();
			} catch (Exception e) {
				logger.error("No form named {} because we could not locate class {}", formId, cls);
				return null;
			}
			this.forms.put(formId, form);
			return form;
		}

		@Override
		public DataType getDataType(String dataTypeId) {
			return this.dataTypes.getDataType(dataTypeId);
		}

		@Override
		public IValueList getValueList(String listId) {
			IValueList list = this.lists.get(listId);
			if (list != null) {
				return list;
			}
			String cls = this.listRoot + toClassName(listId);
			try {
				list = (IValueList) Class.forName(cls).newInstance();
			} catch (Exception e) {
				logger.error("No list named {} because we could not locate class {}", listId, cls);
				return null;
			}
			this.lists.put(listId, list);
			return list;
		}

		@Override
		public Message getMessage(String messageId) {
			return this.messages.getMessage(messageId);
		}

		@Override
		public IService getService(String serviceId) {
			IService service = this.services.get(serviceId);
			if (service != null) {
				return service;
			}
			service = this.tryFormIo(serviceId);
			if (service == null) {
				String cls = this.serviceRoot + toClassName(serviceId);
				try {
					service = (IService) Class.forName(cls).newInstance();
				} catch (Exception e) {
					logger.error("No service named {} because we could not locate class {}", serviceId, cls);
					return null;
				}
			}
			this.services.put(serviceId, service);
			return service;
		}

		private FormIo tryFormIo(String serviceName) {
			int idx = serviceName.indexOf('-');
			if (idx <= 0) {
				return null;
			}

			String formName = serviceName.substring(idx + 1);
			Form fs = this.getForm(formName);
			if (fs == null) {
				return null;
			}

			IoType opern = null;
			try {
				opern = IoType.valueOf(serviceName.substring(0, idx).toUpperCase());
			} catch (Exception e) {
				return null;
			}

			return FormIo.getInstance(opern, serviceName.substring(idx + 1));

		}

	}
}
