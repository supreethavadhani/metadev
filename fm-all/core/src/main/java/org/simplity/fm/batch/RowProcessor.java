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

package org.simplity.fm.batch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.form.Form;
import org.simplity.fm.core.form.FormData;
import org.simplity.fm.core.rdb.DbBatchHandle;
import org.simplity.fm.core.rdb.IDbBatchClient;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IserviceContext;
import org.simplity.fm.core.validn.IValueList;

/**
 * provides meta data as to how to process a row of input data
 * 
 * @author simplity.org
 *
 */
public class RowProcessor {
	protected final FormMapper[] inserts;
	protected final IValueList[] lists;
	protected Map<String, Map<String, String>> simplLists;
	protected Map<String, Map<String, Map<String, String>>> keyedLists;
	

	/**
	 * 
	 * @param inserts
	 *            forms to be inserted
	 * @param lists 
	 */
	public RowProcessor(FormMapper[] inserts, IValueList[] lists) {
		this.inserts = inserts;
		this.lists = lists;
	}

	/**
	 * @param rowProvider
	 * @param ctx
	 * @param validationOnly
	 * @throws SQLException
	 */
	public void process(IRowProvider rowProvider, IserviceContext ctx, boolean validationOnly) throws SQLException {
		if(this.simplLists == null) {
			this.buildLists(ctx);
		}
		if (validationOnly) {
			new Worker(rowProvider, ctx).process();
		} else {
			RdbDriver.getDriver().transactBatch(new DbWorker(rowProvider, ctx));
		}
	}

	private void buildLists(IserviceContext ctx) {
		this.simplLists = new HashMap<>();
		this.keyedLists = new HashMap<>();
		for(IValueList list : this.lists) {
			String name = list.getName().toString();
			if(list.isKeyBased()) {
				this.keyedLists.put(name, list.getAll(ctx));
			}else {
				Map<String, String> map = new HashMap<>();
				this.simplLists.put(name, map);
				for(Object[] row : list.getList(null, ctx)) {
					map.put(row[1].toString(), row[0].toString());
				}
			}
		}
	}

	/*
	 * this is an un-orthodox design. We have clubbed function of two methods
	 * into one.
	 * We considered defining an interface with two methods, one to get the next
	 * row, and the other to give back the result of processing that row. That
	 * way we will be calling them alternately.
	 * However, that design does not reflect that fact that they will be called
	 * alternately.
	 *
	 */
	/**
	 * 
	 * result of the last row operation is provided as a parameter, while the
	 * row for next operation is expected back
	 *
	 */
	@FunctionalInterface
	public interface IRowProvider {
		/**
		 * get next row to be processed.
		 * 
		 * @param messagesForLastRow
		 *            empty if the last row was successful. error message/s in
		 *            case of any error
		 * 
		 * @return next row to be processed. null if there are no more rows
		 */
		public Map<String, String> nextRow(Message[] messagesForLastRow);
	}

	protected class Worker {
		protected final IRowProvider rowProvider;
		protected final IserviceContext ctx;

		protected Worker(IRowProvider rowProvider, IserviceContext ctx) {
			this.rowProvider = rowProvider;
			this.ctx = ctx;
		}

		protected void process() {
			Message[] errors = new Message[0];
			while (true) {
				Map<String, String> values = this.rowProvider.nextRow(errors);
				if (values == null) {
					return;
				}
				this.ctx.resetMessages();
				for (FormMapper mapper : RowProcessor.this.inserts) {
					mapper.loadData(values, this.ctx);
					String kn = mapper.getGeneratedKeyOutputName();
					if (kn != null) {
						/*
						 * we are not doing db operation. dummy key
						 */
						values.put(kn, "1");
					}
				}
				//errors should be supplied to the next call
				errors = this.ctx.getMessages();
			}
		}

	}

	protected class DbWorker implements IDbBatchClient {
		protected final IRowProvider rowProvider;
		protected final IserviceContext ctx;

		protected DbWorker(IRowProvider rowProvider, IserviceContext ctx) {
			this.rowProvider = rowProvider;
			this.ctx = ctx;
		}

		@Override
		public void doBatch(DbBatchHandle handle) throws SQLException {
			handle.setAutoCommitMode(false);
			Message[] errors = new Message[0];
			while (true) {
				Map<String, String> values = this.rowProvider.nextRow(errors);
				if (values == null) {
					return;
				}
				this.ctx.resetMessages();
				boolean allOk = true;
				for (FormMapper mapper : RowProcessor.this.inserts) {
					FormData fd = mapper.loadData(values, this.ctx);
					if (fd == null) {
						allOk = false;
						break;
					}
					allOk = fd.insert(handle);
					if (!allOk) {
						break;
					}
					String kn = mapper.getGeneratedKeyOutputName();
					if (kn != null) {
						String name = fd.getForm().getDbMetaData().generatedColumnName;
						if (name != null) {
							int idx = fd.getFieldIndex(name);
							values.put(kn, "" + fd.getLongValue(idx));
						}
					}
				}
				if (allOk) {
					handle.commit();
				} else {
					handle.rollback();
				}
				errors = this.ctx.getMessages();
			}
		}
	}
	
	protected class FormMapper {
		private final Form form;
		private final String generatedKeyOutputName;
		private final IValueProvider[] valueProviders;

		protected FormMapper(Form form, String generatedKeyOutputName, IValueProvider[] valueProviders) {
			this.form = form;
			this.generatedKeyOutputName = generatedKeyOutputName;
			this.valueProviders = valueProviders;
		}

		/**
		 * @param values
		 * @param ctx
		 *            that must have user and tenantKey if the insert operation
		 *            require these
		 * @return loaded form data. null in case of any error in loading. Actual
		 *         error messages are put into the context
		 */
		public FormData loadData(Map<String, String> values, IserviceContext ctx) {
			String[] data = new String[this.valueProviders.length];
			int idx = 0;
			for (IValueProvider vp : this.valueProviders) {
				if (vp != null) {
					data[idx] = vp.getValue(values);
				}
				idx++;
			}
			FormData fd = this.form.newFormData();
			ctx.resetMessages();
			fd.validateAndLoadForInsert(data, ctx);
			if (ctx.allOk()) {
				return fd;
			}

			return null;
		}

		/**
		 * @return the generatedKeyOutputName
		 */
		public String getGeneratedKeyOutputName() {
			return this.generatedKeyOutputName;
		}
	}
	
}
