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
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.datatypes.DataType;
import org.simplity.fm.core.fn.Average;
import org.simplity.fm.core.fn.Concat;
import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.fn.Max;
import org.simplity.fm.core.fn.Min;
import org.simplity.fm.core.fn.Sum;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.ListService;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses ServiceLoader() to look for a provider. Uses a default empty provider
 * instead of throwing exception. That is, if no provider is available, all
 * requests will responded with null, after logging an error message.
 *
 * @author simplity.org
 *
 */

public class CompProvider implements ICompProvider {
	private static final Logger logger = LoggerFactory.getLogger(CompProvider.class);
	private static final char DOT = '.';
	private static final String RECORD = Conventions.App.RECORD_CLASS_SUFIX;
	private static final String FORM = Conventions.App.FORM_CLASS_SUFIX;

	private final IDataTypes dataTypes;
	private final String formRoot;
	private final String recordRoot;
	private final String listRoot;
	private final String serviceRoot;
	private final String customListRoot;
	private final String fnRoot;
	private final IMessages messages;
	private final Map<String, Form<?>> forms = new HashMap<>();
	private final Map<String, Record> records = new HashMap<>();
	private final Map<String, IValueList> lists = new HashMap<>();
	private final Map<String, IService> services = new HashMap<>();
	private final Map<String, IFunction> functions = new HashMap<>();

	/**
	 * @param rootPackageName
	 * @return instance, or null in case of any errors in creating one for teh
	 *         root package
	 */
	public static CompProvider getPrivider(final String rootPackageName) {

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

	private CompProvider(final IDataTypes dataTypes, final IMessages messages, final String rootPackage) {
		this.dataTypes = dataTypes;
		this.messages = messages;
		final String genRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_GEN + DOT;
		this.formRoot = genRoot + Conventions.App.FOLDER_NAME_FORM + DOT;
		this.recordRoot = genRoot + Conventions.App.FOLDER_NAME_RECORD + DOT;
		this.listRoot = genRoot + Conventions.App.FOLDER_NAME_LIST + DOT;
		this.customListRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_CUSTOM_LIST + DOT;
		this.serviceRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_SERVICE + DOT;
		this.fnRoot = rootPackage + DOT + Conventions.App.FOLDER_NAME_FN + DOT;
		/*
		 * add hard-wired services to the list
		 */
		this.services.put(Conventions.App.SERVICE_LIST, ListService.getInstance());
		/*
		 * add standard functions
		 */
		this.addStandardFuntions();
	}

	private void addStandardFuntions() {
		this.functions.put("concat", new Concat());
		this.functions.put("average", new Average());
		this.functions.put("sum", new Sum());
		this.functions.put("min", new Min());
		this.functions.put("max", new Max());
	}

	@Override
	public Form<?> getForm(final String formId) {
		Form<?> form = this.forms.get(formId);
		if (form != null) {
			return form;
		}
		final String cls = this.formRoot + toClassName(formId) + FORM;
		try {
			form = (Form<?>) Class.forName(cls).newInstance();
		} catch (final ClassNotFoundException e) {
			logger.error("No form named {} because we could not locate class {}", formId, cls);
			return null;
		} catch (final Exception e) {
			logger.error("Internal Error: Form named " + formId
					+ " exists but an excption occured while creating an instance. Error :", e);
			return null;
		}
		this.forms.put(formId, form);
		return form;
	}

	@Override
	public Record getRecord(final String recordName) {
		Record record = this.records.get(recordName);
		if (record != null) {
			return record;
		}
		final String cls = this.recordRoot + toClassName(recordName) + RECORD;
		try {
			record = (Record) Class.forName(cls).newInstance();
		} catch (final ClassNotFoundException e) {
			logger.error("No record named {} because we could not locate class {}", recordName, cls);
			return null;
		} catch (final Exception e) {
			logger.error("Internal Error: record named" + recordName
					+ " exists but an excption occured while while creating an instance. Error :", e);
			return null;
		}
		this.records.put(recordName, record);
		return record;
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
			} catch (final ClassNotFoundException e1) {
				logger.error("No list named {} because we could not locate class {} or {}", listId, cls, cls1);
				return null;
			} catch (final Exception e1) {
				logger.error("Internal Error: List named" + listId
						+ " exists but an excption occured while while creating an instance. Error :", e);
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
		 * we first check for a class. this approach allows us to over-ride
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

		final String formName = serviceName.substring(idx + 1);
		logger.info("Looking to generate a service for operantion {} on {}", opern, formName);

		/*
		 * we provide flexibility for the service name to have Form suffix
		 * at the end, failing which we try the name itself as form
		 */
		if (formName.endsWith(FORM)) {
			final String fn = formName.substring(0, formName.length() - FORM.length());
			final Form<?> form = this.getForm(fn);
			if (form != null) {
				return form.getService(opern);
			}
		}

		final Form<?> form = this.getForm(formName);
		if (form != null) {
			return form.getService(opern);
		}

		logger.info("{} is not a form and hence a service is not generated for oepration {}", formName, opern);
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

	private static String toClassName(final String name) {
		int idx = name.lastIndexOf('.');
		if (idx == -1) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		idx++;
		return name.substring(0, idx) + name.substring(idx, idx + 1).toUpperCase() + name.substring(idx + 1);
	}

}
