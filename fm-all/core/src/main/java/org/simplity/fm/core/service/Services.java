
package org.simplity.fm.core.service;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Forms;
import org.simplity.fm.core.form.IoType;
import org.simplity.fm.core.form.Form;
import org.simplity.fm.core.form.FormIo;

/**
 * Place holder the serves as a source for service instances
 */
public class Services {
	/**
	 * separator between operation and form name to suggest a service name, like
	 * get-form1
	 */
	public static final char SERVICE_SEPARATOR = '-';
	/**
	 * list service
	 */
	public static final String LIST_SERVICE = "listService";

	private static final Services instance = new Services();

	/**
	 * 
	 * @param serviceName
	 * @return service instance for this service name, or null if no such
	 *         service
	 */
	public static IService getService(String serviceName) {
		IService service = instance.services.get(serviceName);
		if (service != null) {
			return service;
		}
		int idx = serviceName.indexOf(SERVICE_SEPARATOR);
		if (idx <= 0) {
			return null;
		}

		String formName = serviceName.substring(idx + 1);
		Form fs = Forms.getForm(formName);
		if (fs == null) {
			return null;
		}

		IoType opern = null;
		try {
			opern = IoType.valueOf(serviceName.substring(0, idx).toUpperCase());
		} catch (Exception e) {
			return null;
		}
		
		service = FormIo.getInstance(opern, serviceName.substring(idx + 1));
		if (service != null) {
			instance.services.put(serviceName, service);
		}
		return service;
	}

	/**
	 * utility for special services to register them selves
	 * 
	 * @param serviceName
	 * @param service
	 */
	public static void registerService(String serviceName, IService service) {
		if (service != null && serviceName != null) {
			instance.services.put(serviceName, service);
		}
	}

	private Map<String, IService> services = new HashMap<>();

	private Services() {
		this.services.put(LIST_SERVICE, ListService.getInstance());
	}
}
