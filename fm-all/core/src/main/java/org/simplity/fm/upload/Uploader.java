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

package org.simplity.fm.upload;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.simplity.fm.core.App;
import org.simplity.fm.core.rdb.TransactionHandle;
import org.simplity.fm.core.service.IServiceContext;

/**
 * @author simplity.org
 *
 */
public class Uploader {
	protected final FormLoader[] inserts;

	Uploader(final FormLoader[] inserts) {
		this.inserts = inserts;
	}

	/**
	 * @param client
	 *            client for this uploader that supplies input rows
	 * @param ctx
	 * @return info about what happened
	 * @throws SQLException
	 */
	public UploadResult upload(final IUploadClient client, final IServiceContext ctx) throws SQLException {
		final Worker worker = new Worker(client, ctx);
		App.getApp().getDbDriver().transact(handle -> {
			worker.transact(handle);
		});
		return worker.getResult();
	}

	/**
	 * @param client
	 *            client for this uploader that supplies input rows
	 * @param ctx
	 *
	 * @return info about what happened
	 */
	public UploadResult validate(final IUploadClient client, final IServiceContext ctx) {
		final Worker worker = new Worker(client, ctx);
		worker.validate();
		return worker.getResult();
	}

	protected class Worker {
		private Instant startedAt;
		private final IUploadClient client;
		private final IServiceContext ctx;
		private Instant doneAt;
		private int nbrRows = 0;
		private int nbrErrors = 0;

		protected Worker(final IUploadClient client, final IServiceContext ctx) {
			this.client = client;
			this.ctx = ctx;
		}

		protected UploadResult getResult() {
			return new UploadResult(this.startedAt, this.doneAt, this.nbrRows, this.nbrErrors, this.ctx.getMessages());
		}

		protected void transact(final TransactionHandle handle) throws SQLException {
			this.startedAt = Instant.now();
			while (true) {
				final Map<String, String> input = this.client.nextRow(this.ctx);
				if (input == null) {
					this.doneAt = Instant.now();
					return;
				}

				this.nbrRows++;
				boolean ok = true;
				for (final FormLoader loader : Uploader.this.inserts) {
					if (!loader.loadData(input, handle, this.ctx)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					handle.commit();
				} else {
					handle.rollback();
					this.nbrErrors++;
				}
			}
		}

		protected void validate() {
			this.startedAt = Instant.now();
			while (true) {
				final Map<String, String> input = this.client.nextRow(this.ctx);
				if (input == null) {
					this.doneAt = Instant.now();
					return;
				}

				this.nbrRows++;
				boolean ok = true;
				for (final FormLoader loader : Uploader.this.inserts) {
					if (!loader.validate(input, this.ctx)) {
						ok = false;
					}
				}

				if (!ok) {
					this.nbrErrors++;
				}
			}
		}
	}
}
