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

package org.simplity.fm.gen;

/**
 * @author simplity.org
 *
 */
public class Gen {
	private static final String SPEC_ROOT = "c:/repos/forms/fm-all/gen/src/test/resources/spec/";
	private static final String JAVA_ROOT = "c:/repos/forms/fm-all/gen/src/test/java/";
	private static final String JAVA_PACKAGE = "org.simplity.fm.test.gen";
	private static final String TS_ROOT = "c:/repos/forms/fm-all/gen/src/test/resources/ts/forms";
	private static final String TS_FORM_IMPORT_PREFIX = "../form/";

	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {

		Generator.generate(SPEC_ROOT, JAVA_ROOT, JAVA_PACKAGE, TS_ROOT, TS_FORM_IMPORT_PREFIX);
	}
}
