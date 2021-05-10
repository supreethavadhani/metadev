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

package org.simplity.fm.core.app;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.simplity.fm.core.conf.CompProvider;
import org.simplity.fm.core.conf.IAccessController;
import org.simplity.fm.core.conf.ICompProvider;
import org.simplity.fm.core.conf.IDbConnectionFactory;
import org.simplity.fm.core.conf.IEmailer;
import org.simplity.fm.core.conf.IExceptionListener;
import org.simplity.fm.core.conf.IRequestLogger;
import org.simplity.fm.core.conf.IServiceContextFactory;
import org.simplity.fm.core.conf.ISessionCache;
import org.simplity.fm.core.conf.ITexter;
import org.simplity.fm.core.conf.defalt.DefaultAccessController;
import org.simplity.fm.core.conf.defalt.DefaultCompProvider;
import org.simplity.fm.core.conf.defalt.DefaultContextFactory;
import org.simplity.fm.core.conf.defalt.DefaultDbConFactory;
import org.simplity.fm.core.conf.defalt.DefaultEmailer;
import org.simplity.fm.core.conf.defalt.DefaultExceptionListener;
import org.simplity.fm.core.conf.defalt.DefaultRequestLogger;
import org.simplity.fm.core.conf.defalt.DefaultSessionCacher;
import org.simplity.fm.core.conf.defalt.DefaultTexter;
import org.simplity.fm.core.rdb.RdbDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a concrete App
 *
 * @author simplity.org
 *
 */
public class App implements IApp {

	protected static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final String UNKNOWN = "_unknown_";
	private static App app = new App();

	/*
	 * initialized as a full-scale dummy!!
	 */
	private String appName = UNKNOWN;
	private ICompProvider compProvider = new DefaultCompProvider();
	private IAccessController guard = new DefaultAccessController();
	private RdbDriver rdbDriver = new RdbDriver(new DefaultDbConFactory());
	private IExceptionListener listener = new DefaultExceptionListener();
	private ISessionCache cache = new DefaultSessionCacher();
	private IRequestLogger reqLogger = new DefaultRequestLogger();
	private ITexter texter = new DefaultTexter();
	private IEmailer emalier = new DefaultEmailer();
	private IServiceContextFactory contextFactory = new DefaultContextFactory();

	/**
	 * @return the app. A dummy app if no App is configured, or if a
	 *         configuration has failed,
	 */
	public static IApp getApp() {
		return app;
	}

	/**
	 * bootstrap based on any IBootstrapper that is configured using the
	 * ServiceLoader java utility
	 */
	public static void bootstrap() {
		final Iterator<AppConfigProvider> iter = ServiceLoader.load(AppConfigProvider.class).iterator();
		if (iter.hasNext()) {
			final AppConfigProvider bs = iter.next();
			configureApp(bs.getConfig());
			if (iter.hasNext()) {
				logger.warn(
						"Found {} as an additional IBootstrapper. Ignoring this as well as any more possible Apps that are available on our path",
						iter.next().getClass().getName());
			}
		} else {
			app.listener.listen(null,
					new ApplicationError("No bootrsappers available on our path. Will continue with a dummy APP"));
		}

	}

	/**
	 * configure the app
	 *
	 * @param config
	 */
	public static void configureApp(final Config config) {
		String text = config.appName;

		if (text == null || text.isEmpty()) {
			logger.error("App name must be a unique name. Moving with name {} ", UNKNOWN);
			app.appName = UNKNOWN;
		} else {
			app.appName = text;
		}

		text = config.appRootPackage;

		if (text == null || text.isEmpty()) {
			logger.error(
					"root package name is required to locate app components. This app will throw exception if any component is requested");
			app.compProvider = new DefaultCompProvider();
		} else {
			app.compProvider = CompProvider.getPrivider(text);
			if (app.compProvider == null) {
				logger.error("Error while initializing comp provider using root package {}", text);
				app.compProvider = new DefaultCompProvider();
			}
		}

		if (config.accessController == null) {
			logger.warn("No access controller configured. All services granted for all users");
			app.guard = new DefaultAccessController();
		} else {
			app.guard = config.accessController;
		}

		if (config.dbConnectionFactory == null) {
			logger.warn("No DB connection configured. No db access");
			app.rdbDriver = new RdbDriver(new DefaultDbConFactory());
		} else {
			app.rdbDriver = new RdbDriver(config.dbConnectionFactory);
		}

		if (config.exceptionListener == null) {
			logger.warn(
					"No exception listener configured. All exceptions will just be logged before responding to the client");
			app.listener = new DefaultExceptionListener();
		} else {
			app.listener = config.exceptionListener;
		}

		if (config.sessionCache == null) {
			logger.warn("No Session Cacher controller configured. local caching arranged instead..");
			app.cache = new DefaultSessionCacher();
		} else {
			app.cache = config.sessionCache;
		}

		if (config.requestLogger == null) {
			logger.warn("No Request logger configured. requests will be merged with general logging..");
			app.reqLogger = new DefaultRequestLogger();
		} else {
			app.reqLogger = config.requestLogger;
		}

		if (config.texter == null) {
			logger.warn(
					"SMS texts can not be sent as the facility is not configured. SMS text will insted be just logged");
			app.texter = new DefaultTexter();
		} else {
			app.texter = config.texter;
		}

		if (config.contextFactory == null) {
			logger.warn("No custom factory is defined to create service context. A default one is used");
			app.contextFactory = new DefaultContextFactory();
		} else {
			app.contextFactory = config.contextFactory;
		}
	}

	@Override
	public int getMaxRowsToExtractFromDb() {
		return 0;
	}

	@Override
	public boolean treatNullAsEmptyString() {
		return false;
	}

	@Override
	public String getName() {
		return this.appName;
	}

	@Override
	public ICompProvider getCompProvider() {
		return this.compProvider;
	}

	@Override
	public RdbDriver getDbDriver() {
		return this.rdbDriver;
	}

	@Override
	public IAccessController getAccessController() {
		return this.guard;
	}

	@Override
	public IExceptionListener getExceptionListener() {
		return this.listener;
	}

	@Override
	public ISessionCache getSessionCache() {
		return this.cache;
	}

	@Override
	public IRequestLogger getRequestLogger() {
		return this.reqLogger;
	}

	@Override
	public ITexter getTexter() {
		return this.texter;
	}
	
	@Override
	public IEmailer getEmailer() {
		return this.emalier;
	}

	@Override
	public IServiceContextFactory getContextFactory() {
		return this.contextFactory;
	}

	/**
	 * This is a data structure to be used to pass parameter values for App
	 * configuration. We prefer to keep this as a simple data-structure than
	 * adding
	 * setters/getters or making it immutable with builder-pattern etc..
	 *
	 * @author simplity.org
	 *
	 */
	public static class Config {
		/**
		 * must be set to a unique name
		 */
		public String appName;

		/**
		 * root package inside which the generated components are expected. This
		 * is
		 * generally the root package of the app, like "com.myCompany.myApp". In
		 * this case records are found with package name
		 * "com.myCompany.myApp.gen.rec"
		 *
		 * If this is not specified, then any request for component will result
		 * in
		 * an exception
		 */
		public String appRootPackage;

		/**
		 * optional. Instance that is called to check if the logged-in user is
		 * authorized for the requested service.
		 */
		public IAccessController accessController;

		/**
		 * optional. if not set, any request for db access will result in an
		 * exception
		 */

		public IDbConnectionFactory dbConnectionFactory;

		/**
		 * optional.
		 */
		public IExceptionListener exceptionListener;

		/**
		 * optional. a simple map-based cacher is used. Entries do not expire
		 */
		public ISessionCache sessionCache;

		/**
		 * optional. requests are logged using the underlying logger-framework
		 */

		public IRequestLogger requestLogger;

		/**
		 * optional. if not specified, text messages are just logged.
		 */
		public ITexter texter;
		
		/**
		 * optional. if not specified, emails  are just logged.
		 */
		public IEmailer emailer;

		/**
		 * optional. if not specified, Default context is created
		 */
		public IServiceContextFactory contextFactory;
	}

}