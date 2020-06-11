package com.parse.models;

import java.util.List;

public class ProcessedStatementInfo {

	private String convertedStatement;
	private String predicateStatement;
	private List<String> predicates;

	public String getConvertedStatement() {
		return convertedStatement;
	}

	public void setConvertedStatement(String convertedStatement) {
		this.convertedStatement = convertedStatement;
	}

	public String getPredicateStatement() {
		return predicateStatement;
	}

	public void setPredicateStatement(String predicateStatement) {
		this.predicateStatement = predicateStatement;
	}

	public List<String> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<String> predicates) {
		this.predicates = predicates;
	}
}
