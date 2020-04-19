package com.parse.models;

import java.util.HashMap;

/**
 * The model ReplacementInfo. It holds string replacement information of a
 * statement.
 */
public class ReplacementInfo {

	private String updatedString;
	private HashMap<String, String> replacementMap;

	public ReplacementInfo(String updatedString, HashMap<String, String> replacementMap) {
		super();
		this.updatedString = updatedString;
		this.replacementMap = replacementMap;
	}

	public String getUpdatedString() {
		return updatedString;
	}

	public void setUpdatedString(String updatedString) {
		this.updatedString = updatedString;
	}

	public HashMap<String, String> getReplacementMap() {
		return replacementMap;
	}

	public void setReplacementMap(HashMap<String, String> replacementMap) {
		this.replacementMap = replacementMap;
	}
}
