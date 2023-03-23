package org.simplity.fm.gen;

import java.util.HashMap;

public class BuiltInTags {
	HashMap<String, String> builtInTags = new HashMap<>();

	BuiltInTags() {
		builtInTags.put("form", "app-mv-form-generator");
		builtInTags.put("table", "app-mv-table");
		builtInTags.put("Primary", "app-mv-primary-button");
		builtInTags.put("Secondary", "app-mv-secondary-button");
		builtInTags.put("Create", "create");
		builtInTags.put("Update", "save");
		builtInTags.put("Navigate", "navigate");
	}

	public String getValue(String key) {
		return builtInTags.get(key);
	}
}
