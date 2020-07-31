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

package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.RdbDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Utility for managing record overrides
 *
 * @author simplity.org
 *
 */
public class OverrideUtil {
	private static final Logger logger = LoggerFactory.getLogger(OverrideUtil.class);
	private static final String READ_OVR = "select forms, records from st_overrides where id=?";
	private static final ValueType[] OVR_TYPES = { ValueType.Text, ValueType.Text };
	private static final String UPDATE_OVR = "update st_overrides set forms=?, records=? where id=?";
	private static final String INSERT_OVR = "insert into st_overrides (forms, records, id) values (?,?,?)";
	private static final String DELETE_OVR = "delete from st_overrides where id=?";

	private static final String READ_REC = "select json from st_rec_overrides where id=? and name=?";
	private static final ValueType[] REC_TYPES = { ValueType.Text };
	private static final String UPDATE_REC = "update st_rec_overrides set json=? where id=? and name=?";
	private static final String INSERT_REC = "insert into st_rec_overrides(json, id, name) values(?,?,?)";
	private static final String DELETE_REC = "delete from st_rec_overrides where id=? and name=?";

	private static final String COMMA = ",";

	/**
	 * data structure that carries form and record overrides
	 *
	 * @author simplity.org
	 *
	 */
	public static class Overrides {
		/**
		 * form overrides
		 */
		public final String[] forms;
		/**
		 * record overrides
		 */
		public final String[] records;

		/**
		 * final constructor with all attributes
		 *
		 * @param forms
		 * @param records
		 */
		public Overrides(final String[] forms, final String[] records) {
			this.forms = forms;
			this.records = records;
		}
	}

	/**
	 *
	 * @param id
	 * @return overrides, or null if no overrides defined for this id
	 */
	public static Overrides getOverides(final String id) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { id };
		final Overrides[] overs = new Overrides[1];

		try {
			driver.read(handle -> {
				final Object[] result = handle.read(READ_OVR, values, OVR_TYPES);
				if (result == null) {
					logger.info("No overrides for key {}", id);
				} else {
					final String[] forms = ((String) result[0]).split(COMMA);
					final String[] records = ((String) result[1]).split(COMMA);
					overs[0] = new Overrides(forms, records);
				}

			});
		} catch (final SQLException e) {
			throw new ApplicationError("Error while accessing overrides", e);
		}

		return overs[0];
	}

	/**
	 *
	 * @param id
	 * @param overs
	 */
	public static void saveOverides(final String id, final Overrides overs) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { String.join(COMMA, overs.forms), String.join(COMMA, overs.records), id };

		try {
			driver.readWrite(handle -> {
				if (handle.write(UPDATE_OVR, values) > 0) {
					/*
					 * update done
					 */
					return true;
				}
				/*
				 * non-existing row? let us insert
				 */
				if (handle.write(INSERT_OVR, values) > 0) {
					return true;
				}
				/*
				 * no exception but no result!!
				 */
				final String msg = "Error while saving into st_overrides. update/insert failed with no errors";
				logger.error(msg);
				throw new ApplicationError(msg);

			});
		} catch (

		final SQLException e) {
			throw new ApplicationError("Error while saving overrides", e);
		}
	}

	/**
	 *
	 * @param id
	 */
	public static void deleteOverides(final String id) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { id };

		try {
			driver.readWrite(handle -> {
				if (handle.write(DELETE_OVR, values) == 0) {
					/*
					 * it is okay if n == 0, it means that it is not there
					 */
					logger.warn("delete from st_overrides faield for id {} failed. Probably it was not there.");
				}
				/*
				 * in any case we commit
				 */
				return true;
			});
		} catch (final SQLException e) {
			throw new ApplicationError("Error while saving overrides", e);
		}
	}

	/**
	 *
	 * @param id
	 * @param recordName
	 * @param recordJson
	 */
	public static void saveRecord(final String id, final String recordName, final String recordJson) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { recordJson, id, recordName };

		try {
			driver.readWrite(handle -> {
				if (handle.write(UPDATE_REC, values) > 0) {
					/*
					 * updated.
					 */
					return true;
				}
				/*
				 * non existing row? let us inser
				 */
				if (handle.write(INSERT_REC, values) > 0) {
					return true;
				}
				final String msg = "Error while saving into st_rec_overrides. update/insert failed with no errors";
				logger.error(msg);
				throw new ApplicationError(msg);

			});
		} catch (final SQLException e) {
			throw new ApplicationError("Error while saving record override", e);
		}
	}

	/**
	 *
	 * @param id
	 * @param override
	 */
	public static void saveRecord(final String id, final RecordOverride override) {
		saveRecord(id, override.name, new Gson().toJson(override));
	}

	/**
	 *
	 * @param id
	 * @param recordName
	 */
	public static void deleteRecord(final String id, final String recordName) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { id, recordName };

		try {
			driver.readWrite(handle -> {
				if (handle.write(DELETE_REC, values) == 0) {
					/*
					 * it is okay if n == 0, it means that it is not there
					 */
					logger.warn("delete from st_rec_overrides faield for id {} failed. Probably it was not there.");
				}
				return true;
			});
		} catch (final SQLException e) {
			throw new ApplicationError("Error while saving overrides", e);
		}
	}

	/**
	 *
	 * @param id
	 * @param recordName
	 * @return instance of record override. null if this is not found.
	 */
	public static RecordOverride getRecord(final String id, final String recordName) {
		final RdbDriver driver = App.getApp().getDbDriver();
		final Object[] values = { id, recordName };
		final String[] texts = new String[1];

		try {
			driver.read(handle -> {
				final Object[] result = handle.read(READ_REC, values, REC_TYPES);
				if (result != null) {
					texts[0] = (String) result[0];
				}

			});
		} catch (final SQLException e) {

			throw new ApplicationError("Error while accessing overrides", e);
		}

		final String text = texts[0];
		if (text == null) {
			return null;
		}

		/*
		 * this text is the json for RecordOverride instance
		 */
		return new Gson().fromJson(text, RecordOverride.class);
	}
}
