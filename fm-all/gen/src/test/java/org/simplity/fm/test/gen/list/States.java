package org.simplity.fm.test.gen.list;

import java.util.HashMap;
import org.simplity.fm.core.validn.KeyedValueList;
import org.simplity.fm.core.validn.ValueList;
public class States extends KeyedValueList {
	private static final Object[] KEYS = {130L
		};
	private static final Object[][][] VALUES = {
			{
				{"Karnataka", null}, 
				{"Tamil Nadu", null}, 
				{"Kerala", null}, 
				{"Uttar Pradesh", null}
			}};
	private static final String NAME = "states";

/**
 *states
 */
	public States() {
		this.name = NAME;
		this.values = new HashMap<>();
		for (int i = 0; i < KEYS.length;i++) {
			this.values.put(KEYS[i], new ValueList(KEYS[i], VALUES[i]));
		}
	}
}
