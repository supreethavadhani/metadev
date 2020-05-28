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

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.data.DbTable;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.serialize.ISerializer;

import com.google.gson.stream.JsonWriter;

/**
 * highly restrictive implementation that just about serves our purpose.
 *
 * @author simplity.org
 *
 */
@SuppressWarnings("resource")
public class JsonSerializer implements ISerializer {
	private static final String NULL = "";
	private final JsonWriter writer;

	/**
	 * serializer is meant to create a string, and not write directly to a
	 * stream.
	 *
	 * @param sw
	 */
	public JsonSerializer(final StringWriter sw) {
		this.writer = new JsonWriter(sw);
	}

	@Override
	public void beginObject() {
		try {
			this.writer.beginObject();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void endObject() {
		try {
			this.writer.endObject();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void beginArray() {
		try {
			this.writer.beginArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void endArray() {
		try {
			this.writer.endArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void name(final String name) {
		try {
			this.writer.name(name);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final String value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final long value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final boolean value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final double value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final LocalDate value) {
		/*
		 * we may have to add a specific formatter in the future
		 */
		try {
			this.writer.value(value.toString());
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void value(final Instant value) {
		/*
		 * we may have to add a specific formatter in the future
		 */
		try {
			this.writer.value(value.toString());
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void primitiveObject(final Object primitive) {
		try {
			if (primitive == null) {
				this.writer.value(NULL);
				return;
			}

			if (primitive instanceof Boolean) {
				this.writer.value(((Boolean) primitive).booleanValue());
				return;
			}

			if (primitive instanceof Number) {
				this.writer.value((Number) primitive);
				return;
			}

			this.writer.value(primitive.toString());
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void fields(final Field[] fields, final Object[] values) {
		try {
			for (int i = 0; i < fields.length; i++) {
				this.writer.name(fields[i].getName());
				this.primitiveObject(values[i]);
			}
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void fields(final Record record) {
		this.fields(record.fetchFields(), record.fetchRawData());

	}

	@Override
	public void array(final String memberName, final Field[] fields, final Object[][] rows) {
		try {
			this.writer.name(memberName);
			this.writer.beginArray();
			if (rows != null && rows.length > 0) {
				this.arrayElements(fields, rows);
			}
			this.writer.endArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void arrayElements(final Field[] fields, final Object[][] rows) {
		try {
			for (final Object[] row : rows) {
				this.writer.beginObject();
				for (int i = 0; i < fields.length; i++) {
					this.writer.name(fields[i].getName());
					this.primitiveObject(row[i]);
				}
				this.writer.endObject();
			}
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void array(final String memberName, final DbTable<?> table) {
		try {
			this.writer.name(memberName);
			this.writer.beginArray();
			if (table != null && table.length() > 0) {
				this.arrayElements(table);
			}
			this.writer.endArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public void arrayElements(final DbTable<?> table) {
		table.forEach(rec -> {
			try {
				this.writer.beginObject();
				this.fields(rec);
				this.writer.endObject();
			} catch (final IOException e) {
				throw new ApplicationError("", e);
			}
		});
	}
}
