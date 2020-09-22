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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
class KeyedList implements Util.ISelfLoader {
	private static final String C = ", ";
	String name;
	Map<String, ValueList> lists = new HashMap<>();

	@Override
	public void fromJson(final JsonReader reader, final String key, final int idx) throws IOException {
		this.name = key;
		Util.loadMap(this.lists, reader, ValueList.class);
	}

	void emitJava(final StringBuilder sbf, final String packageName) {
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, HashMap.class);
		Util.emitImport(sbf, org.simplity.fm.core.validn.KeyedValueList.class);
		Util.emitImport(sbf, org.simplity.fm.core.validn.ValueList.class);

		sbf.append("\npublic class ").append(Util.toClassName(this.name)).append(" extends KeyedValueList {");

		sbf.append("\n\tprivate static final Object[] KEYS = {");
		final StringBuilder vals = new StringBuilder();
		boolean firstOne = true;
		boolean keyIsNumeric = false;
		vals.append("\n\tprivate static final Object[][][] VALUES = {");
		for (final Map.Entry<String, ValueList> entry : this.lists.entrySet()) {
			final String key = entry.getKey();
			if (firstOne) {
				try {
					Long.parseLong(key);
					keyIsNumeric = true;
				} catch (final Exception e) {
					// key is not numeric
				}
				firstOne = false;
			}
			if (keyIsNumeric) {
				sbf.append(key).append('L');
			} else {
				sbf.append(Util.escape(key.toString()));
			}
			sbf.append(C);
			emitJavaSet(vals, entry.getValue());
			vals.append(C);
		}
		sbf.setLength(sbf.length() - C.length());
		sbf.append("\n\t\t};");

		vals.setLength(vals.length() - C.length());
		vals.append("};");
		sbf.append(vals.toString());

		sbf.append("\n\tprivate static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\n/**\n *").append(this.name).append("\n */");

		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.values = new HashMap<>();");

		sbf.append("\n\t\tfor (int i = 0; i < KEYS.length;i++) {");
		sbf.append("\n\t\t\tthis.values.put(KEYS[i], new ValueList(KEYS[i], VALUES[i]));");
		sbf.append("\n\t\t}");
		sbf.append("\n\t}");
		sbf.append("\n}\n");
	}

	private static void emitJavaSet(final StringBuilder vals, final ValueList list) {
		final Pair[] ps = list.pairs;
		vals.append("\n\t\t\t{");
		for (final Pair p : ps) {
			vals.append("\n\t\t\t\t{");
			if (p.value instanceof String) {
				vals.append(Util.escape(p.value.toString()));
			} else {
				vals.append(p.value).append('L');
			}
			vals.append(C).append(Util.escape(p.label)).append("}");
			vals.append(C);
		}
		vals.setLength(vals.length() - C.length());
		vals.append("\n\t\t\t}");
	}

	protected void emitTs(final StringBuilder sbf, final String indent) {
		boolean firstOne = true;
		for (final Map.Entry<String, ValueList> entry : this.lists.entrySet()) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
			}
			sbf.append(indent);
			sbf.append(entry.getKey()).append(" : [");
			final String newIndent = indent + '\t';
			boolean f = true;
			for (final Pair p : entry.getValue().pairs) {
				if (f) {
					f = false;
				} else {
					sbf.append(C);
				}
				sbf.append(newIndent).append("{value:");
				if (p.value instanceof String) {
					sbf.append(Util.escapeTs(p.value));
				} else {
					sbf.append(p.value);
				}
				sbf.append(",text:").append(Util.escapeTs(p.label)).append("}");
			}
			sbf.append(indent).append(']');
		}
	}

	/**
	 * @param sbf
	 */
	public void emitNewTs(final StringBuilder sbf) {
		sbf.append("\n\t").append(this.name).append(": {");
		sbf.append("\n\t\tname: '").append(this.name).append("',");
		sbf.append("\n\t\tisKeyed: true,");
		sbf.append("\n\t\tkeyedLists: {");

		boolean firstOne = true;
		for (final Map.Entry<String, ValueList> entry : this.lists.entrySet()) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
			}
			sbf.append("\n\t\t\t'").append(entry.getKey()).append("': [");
			boolean f = true;
			for (final Pair p : entry.getValue().pairs) {
				if (f) {
					f = false;
				} else {
					sbf.append(C);
				}
				sbf.append("\n\t\t\t\t{");
				sbf.append("\n\t\t\t\t\tvalue:");
				if (p.value instanceof String) {
					sbf.append(Util.escapeTs(p.value));
				} else {
					sbf.append(p.value);
				}
				sbf.append(",\n\t\t\t\t\ttext:").append(Util.escapeTs(p.label));
				sbf.append("\n\t\t\t\t}");
			}
			sbf.append("\n\t\t\t]");
		}

		sbf.append("\n\t\t}\n\t}");
	}
}