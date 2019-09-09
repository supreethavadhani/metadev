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

/**
 * one place where all constants across layers are defined
 * 
 * @author simplity.org
 *
 */
public class Conventions {
	/**
	 * HTTP related
	 */
	public static class Http {

		/**
		 * header name to specify the service name
		 */
		public static final String SERVICE_HEADER = "_s";
		/**
		 * header name with which token is sent
		 */
		public static final String TOKEN_HEADER = "_t";
		/**
		 * standard header for auth token
		 */
		public static final String AUTH_HEADER = "Authorization";
		/**
		 * various headers that we respond back with
		 */
		public static final String[] HDR_NAMES = { "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
				"Access-Control-Max-Age", "Connection", "Cache-Control", "Expires", "Accept" };
		/**
		 * values for the headers
		 */
		public static final String[] HDR_TEXTS = { "POST, GET, OPTIONS",
				"content-type, " + SERVICE_HEADER + ", " + TOKEN_HEADER, "1728", "Keep-Alive",
				"no-cache, no-store, must-revalidate", "11111110", "application/json" };
		/**
		 * http status
		 */
		public static final int STATUS_ALL_OK = 200;
		/**
		 * http status
		 */
		public static final int STATUS_AUTH_REQUIRED = 401;
		/**
		 * http status
		 */
		public static final int STATUS_INVALID_SERVICE = 404;
		/**
		 * http status
		 */
		public static final int STATUS_METHOD_NOT_ALLOWED = 405;
		/**
		 * http status
		 */
		public static final int STATUS_INVALID_DATA = 406;
		/**
		 * http status
		 */
		public static final int STATUS_INTERNAL_ERROR = 500;

		/**
		 * tag/name of form header in the request pay load
		 */
		public static final String TAG_HEADER = "header";
		/**
		 * tag/name of form data in the request/response pay load
		 */
		public static final String TAG_DATA = "data";
		/**
		 * tag/field name in response payload that is set to true/false
		 */
		public static final String TAG_ALL_OK = "allOk";

		/**
		 * tag/field name in response payload that has an array of messages.
		 */
		public static final String TAG_MESSAGES = "messages";
		/**
		 * tag/attribute/field name in the payload for a lost of rows being
		 * sent/returned
		 */
		public static final String TAG_LIST = "list";
		/**
		 * number of rows of data (expected or delivered)
		 */
		public static final String TAG_NBR_ROWS = "nbrRows";
		/**
		 * filter conditions
		 */
		public static final String TAG_CONDITIONS = "conditions";
		/**
		 * default MAX nbr rows
		 */
		public static final int DEFAULT_NBR_ROWS = 200;
		
	}

	/**
	 * comparators, typically used in expressions and row selection criteria
	 *
	 * @author simplity.org
	 *
	 */
	public static class Comparator {
		/** */
		public static final String EQ = "=";
		/** */
		public static final String NE = "!=";
		/** */
		public static final String LT = "<";
		/** */
		public static final String LE = "<=";
		/** */
		public static final String GT = ">";
		/** */
		public static final String GE = ">=";
		/** */
		public static final String CONTAINS = "~";
		/** */
		public static final String STARTS_WITH = "^";
		/** */
		public static final String BETWEEN = "><";

		/** one of the entries in a list */
		public static final String IN_LIST = "@";
	}
	
	/**
	 * 
	 */
	public static class App{
		/**
		 * file name that has he application level components
		 */
		public static final String APP_FILE = "application.xlsx";
		/**
		 * all data types defined in the app are put into this generated class
		 */
		public static final String GENERATED_DATA_TYPES_CLASS_NAME = "DefinedDataTypes";
		
	}
}
