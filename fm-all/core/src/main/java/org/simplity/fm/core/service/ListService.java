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

package org.simplity.fm.core.service;

import java.io.IOException;
import java.io.Writer;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


/**
 * handles request to get drop-down values for a field, typically from a client
 * @author simplity.org
 *
 */
public class ListService implements IService{
	private static final ListService instance = new ListService();
	protected static final Logger logger = LoggerFactory.getLogger(ListService.class);

	/**
	 * 
	 * @return non-null instance
	 */
	public static ListService getInstance() {
		return instance;
	}
	
	private ListService() {
		//privatised for a singleton pattern
	}


	@Override
	public String getId() {
		return Conventions.App.SERVICE_LIST;
	}
	@Override
	public void serve(IserviceContext ctx, JsonObject payload) throws Exception {
		String listName = null;
		JsonPrimitive ele = payload.getAsJsonPrimitive("list");
		if(ele != null) {
			listName = ele.getAsString();
		}
		if(listName == null || listName.isEmpty()) {
			ctx.addMessage(Message.newError("list is required for listService"));
			return;
		}
		IValueList list = ComponentProvider.getProvider().getValueList(listName);
		if(list == null) {
			ctx.addMessage(Message.newError("list " + listName + " is not configured"));
			return;
		}
		String key = null;
		if(list.isKeyBased()) {
			ele = payload.getAsJsonPrimitive("key");
			if(ele != null) {
				key = ele.getAsString();
			}
			if(key == null || key.isEmpty()) {
				ctx.addMessage(Message.newError("list " + listName + " is key based. key is missing in the request"));
				return;
			}
		}
		Object[][] result = list.getList(key, ctx);
		if(result == null || result.length == 0 ) {
			ctx.addMessage(Message.newError("list " + listName + " did not return any values for key "+ key));
			return;
		}
		
		@SuppressWarnings("resource")
		Writer writer = ctx.getResponseWriter();
		writer.write("{\"");
		writer.write(Conventions.Http.TAG_LIST);
		writer.write("\":[");
		boolean firstOne = true;
		for(Object[] row : result) {
			if(firstOne) {
			firstOne = false;
			
			} else {
				writer.write(',');
			}
			writeRow(writer, row);
		}
		writer.write("]}");
	}
	
	private static void  writeRow(Writer writer, Object[] row) throws IOException {
		writer.write("{\"value\":");
		Object val = row[0];
		if(val instanceof Number || val instanceof Boolean) {
			writer.write(val.toString());
		}else {
			writer.write('"');
			writer.write(val.toString());
			writer.write('"');
		}
		writer.write(",text:\"");
		writer.write(row[1].toString());
		writer.write("\"}");
	}
}
