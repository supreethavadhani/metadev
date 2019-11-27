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

package org.simplity.fm.core.data;

/**
 * @author simplity.org
 *
 */
public enum ColumnType {
	/**
	 * primary key as per RDBMS, that is not internally generated
	 */
	PrimaryKey {
		@Override
		public boolean isUpdated() {
			return false;
		}
	},
	/**
	 * primary key field that is generated by the RDBMS. isRequired() is true is
	 * false for insert, and true for other cases. This is handled specifically
	 * by the code
	 */
	GeneratedPrimaryKey {
		@Override
		public boolean isUpdated() {
			return false;
		}

		@Override
		public boolean isInserted() {
			return false;
		}
	},
	/**
	 * tenant key is used in a multi-tenant product design.
	 */
	TenantKey {
		@Override
		public boolean isUpdated() {
			return false;
		}

		@Override
		public boolean isRequired() {
			return false;
		}
	},
	/**
	 * id of the user who created this field. (one of the "standard fields")
	 */
	CreatedBy {
		@Override
		public boolean isUpdated() {
			return false;
		}

		@Override
		public boolean isRequired() {
			return false;
		}
	},
	/**
	 * time-stamp at creation. (one of the "standard fields"). This is not set
	 * using a parameter, but using a sql-command
	 */
	CreatedAt {
		@Override
		public boolean isUpdated() {
			return false;
		}

		@Override
		public boolean isRequired() {
			return false;
		}
	},
	/**
	 * id of user who modified this last. (one of the "standard fields")
	 */
	ModifiedBy {
		@Override
		public boolean isRequired() {
			return false;
		}
	},
	/**
	 * time-stamp for last modification. (one of the "standard fields"). This is
	 * not set using a parameter, but using a sql-command
	 */
	ModifiedAt {
		@Override
		public boolean isRequired() {
			return false;
		}
	},
	/**
	 * data column, not key or special field
	 */
	RequiredData,
	/**
	 * data column, not key or special field
	 */
	OptionalData {
		@Override
		public boolean isRequired() {
			return false;
		}
	};

	/**
	 *
	 * @return true if this column is to be included in the SQL for update.
	 *         For example createdUser field should not be updated
	 */
	@SuppressWarnings("static-method")
	public boolean isUpdated() {
		return true;
	}

	/**
	 * @return true if this column is to be included in the SQL for insert.
	 *         For example time-stamp field is not explicitly set in the sql.
	 *         Also, if the primary is generated, then it is not part of the sql
	 *         statement
	 */
	@SuppressWarnings("static-method")
	public boolean isInserted() {
		return true;
	}

	/**
	 * @return true if value is required from the client. false if it is
	 *         optional. This is used only if isInput() is true. This attribute
	 *         is not applicable if isInput() is false;
	 */
	@SuppressWarnings("static-method")
	public boolean isRequired() {
		return true;
	}
}
