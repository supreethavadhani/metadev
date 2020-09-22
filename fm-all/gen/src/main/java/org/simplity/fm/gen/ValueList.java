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

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
class ValueList implements Util.ISelfLoader {
	private static final String C = ", ";

	String name;
	Pair[] pairs;

	@Override
	public void fromJson(final JsonReader reader, final String key, final int idx) {
		this.name = key;
		this.pairs = Util.GSON.fromJson(reader, Pair[].class);
	}

	void emitJava(final StringBuilder sbf, final String packageName) {
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, org.simplity.fm.core.validn.ValueList.class);

		sbf.append("\npublic class ").append(Util.toClassName(this.name)).append(" extends ValueList {");

		sbf.append("\n\t private static final Object[][] VALUES = { ");
		for (final Pair p : this.pairs) {
			sbf.append("\n\t\t{");
			if (p.value instanceof String) {
				sbf.append(Util.escape(p.value.toString()));
			} else {
				sbf.append(p.value).append('L');
			}
			sbf.append(C).append(Util.escape(p.label)).append("}");
			sbf.append(C);
		}
		sbf.setLength(sbf.length() - C.length());
		sbf.append("\n\t};");
		sbf.append("\n\t private static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\n/**\n *\n\t * @param name\n\t * @param valueList\n */");
		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("(String name, Object[][] valueList) {");
		sbf.append("\n\t\tsuper(name, valueList);");
		sbf.append("\n\t}");

		sbf.append("\n\n/**\n *").append(this.name).append("\n */");
		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tsuper(NAME, VALUES);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");
	}

	protected void emitTs(final StringBuilder sbf, final String indent) {
		for (int i = 0; i < this.pairs.length; i++) {
			if (i != 0) {
				sbf.append(',');
			}
			sbf.append(indent);
			final Pair pair = this.pairs[i];
			sbf.append("{value:");
			if (pair.value instanceof String) {
				sbf.append(Util.escapeTs(pair.value));
			} else {
				sbf.append(pair.value);
			}
			sbf.append(",text:").append(Util.escapeTs(pair.label)).append('}');
		}
	}

	protected void emitNewTs(final StringBuilder sbf) {
		sbf.append("\n\t").append(this.name).append(": {");
		sbf.append("\n\t\tname: '").append(this.name).append("',");
		sbf.append("\n\t\tlist: [");

		for (int i = 0; i < this.pairs.length; i++) {
			if (i != 0) {
				sbf.append(',');
			}
			sbf.append("\n\t\t\t{");
			final Pair pair = this.pairs[i];
			sbf.append("\n\t\t\t\tvalue:");
			if (pair.value instanceof String) {
				sbf.append(Util.escapeTs(pair.value));
			} else {
				sbf.append(pair.value);
			}
			sbf.append(",\n\t\t\t\ttext:").append(Util.escapeTs(pair.label));
			sbf.append("\n\t\t\t}");
		}

		sbf.append("\n\t\t]\n\t}");

	}

}