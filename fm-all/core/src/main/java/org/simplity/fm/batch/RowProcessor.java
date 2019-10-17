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
	protected final IValueList[] systemLists;
	protected final Map<String, Map<String, String>> valueLists;
	

	/**
	 * 
	 * @param inserts
	 *            forms to be inserted
	 * @param valueLists value lists defined for this processors
	 * @param systemLists app/system defined lists
	 */
	public RowProcessor(FormMapper[] inserts, Map<String, Map<String, String>> valueLists, IValueList[] systemLists) {
		this.inserts = inserts;
		this.valueLists = valueLists;
		this.systemLists = systemLists;
	}

	protected Map<String, Map<String, String>> getAllLists(IserviceContext ctx){
		final Map<String, Map<String, String>> result = new HashMap<>();
		result.putAll(this.valueLists);
		
		for(IValueList list : this.systemLists) {
			String name = list.getName().toString();
			result.put(name, list.getAll(ctx));
		}
		return result;
	}
	/**
	 * @param rowProvider
	 * @param ctx
	 * @param validationOnly
	 * @throws SQLException
	 */
	public void process(IRowProvider rowProvider, IserviceContext ctx, boolean validationOnly) throws SQLException {
		
		if (validationOnly) {
			new Worker(rowProvider, ctx).process();
		} else {
			RdbDriver.getDriver().transactBatch(new DbWorker(rowProvider, ctx));
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
		protected final Map<String, Map<String, String>> allLists;

		protected Worker(IRowProvider rowProvider, IserviceContext ctx) {
			this.rowProvider = rowProvider;
			this.ctx = ctx;
			this.allLists = RowProcessor.this.getAllLists(ctx);
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
					mapper.loadData(values, this.allLists, this.ctx);
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
		protected final Map<String, Map<String, String>> allLists;

		protected DbWorker(IRowProvider rowProvider, IserviceContext ctx) {
			this.rowProvider = rowProvider;
			this.ctx = ctx;
			this.allLists = RowProcessor.this.getAllLists(ctx);
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
					FormData fd = mapper.loadData(values, this.allLists, this.ctx);
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
	
	
}
