/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.serialize.gson;

import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.serialize.IInputArray;
import org.simplity.fm.core.serialize.IInputObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * IInputObject implementation using Gson
 *
 * @author simplity.org
 *
 */
public class JsonInputObject implements IInputObject {
	private final JsonObject json;

	/**
	 * create an empty input object
	 */
	public JsonInputObject() {
		this.json = new JsonObject();
	}

	/**
	 * crate an input object based on this json object
	 *
	 * @param json
	 *            must be non-null
	 */
	public JsonInputObject(final JsonObject json) {
		if (json == null) {
			throw new ApplicationError("JsonInputObject requires non-null json.");
		}
		this.json = json;
	}

	@Override
	public IInputObject getObject(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonObject()) {
			return (new JsonInputObject((JsonObject) ele));
		}
		return null;
	}

	@Override
	public IInputArray getArray(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonArray()) {
			return (new JsonInputArray((JsonArray) ele));
		}
		return null;
	}

	private JsonPrimitive getPrimitive(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonPrimitive()) {
			return (JsonPrimitive) ele;
		}
		return new JsonPrimitive("");
	}

	@Override
	public long getLong(final String name) {
		try {
			return this.getPrimitive(name).getAsLong();
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String getString(final String name) {
		return this.getPrimitive(name).getAsString();
	}

	@Override
	public boolean getBoolean(final String name) {
		return this.getPrimitive(name).getAsBoolean();
	}

	@Override
	public double getDecimal(final String name) {
		try {
			return this.getPrimitive(name).getAsDouble();
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public IInputObject getEmptyObject() {
		return new JsonInputObject();
	}

	@Override
	public IInputArray getEmptyArray() {
		return new JsonInputArray();
	}

	@Override
	public boolean isEmpty() {
		return this.json.size() == 0;
	}

	@Override
	public Iterable<String> names() {
		return this.json.keySet();
	}

}
