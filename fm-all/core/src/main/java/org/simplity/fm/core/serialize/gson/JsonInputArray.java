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

import java.util.Iterator;
import java.util.function.Function;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.serialize.IInputArray;
import org.simplity.fm.core.serialize.IInputObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class JsonInputArray implements IInputArray {
	private final JsonArray array;

	/**
	 * create an empty input-array
	 */
	public JsonInputArray() {
		this.array = new JsonArray();
	}

	/**
	 *
	 * @param array
	 *            must be non-null
	 */
	public JsonInputArray(final JsonArray array) {
		if (array == null) {
			throw new ApplicationError("JsonInputArray requires non-null array.");
		}
		final int nbr = array.size();
		for (int i = 0; i < nbr; i++) {
			if (array.get(i).isJsonObject() == false) {
				throw new ApplicationError("JsonInputArray contains a non-object member at " + i);
			}
		}
		this.array = array;
	}

	@Override
	public int length() {
		return this.array.size();
	}

	@Override
	public void forEach(final Function<IInputObject, Boolean> fn) {
		final boolean[] isStopped = { false };
		this.array.forEach(ele -> {
			if (isStopped[0]) {
				return;
			}

			final JsonInputObject obj = new JsonInputObject((JsonObject) ele);
			if (fn.apply(obj)) {
				// true means we should continue to iterate
			} else {
				isStopped[0] = true;
			}
		});
	}

	@Override
	public Iterator<IInputObject> iterator() {
		final JsonArray arr = this.array;
		return new Iterator<IInputObject>() {
			private int idx = 0;

			@Override
			public IInputObject next() {
				return new JsonInputObject((JsonObject) arr.get(this.idx++));
			}

			@Override
			public boolean hasNext() {
				return this.idx < arr.size();
			}
		};
	}
}
