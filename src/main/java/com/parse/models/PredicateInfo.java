package com.parse.models;

/**
 * The model PredicateInfo. It holds the predicate information.
 */
public class PredicateInfo {

	/**
	 * The predicate name
	 */
	private String name;

	/**
	 * The predicate type
	 */
	private String type;

	/**
	 * The predicate control
	 */
	private String control;

	/**
	 * The predicate initialization statement
	 */
	private String predicateInitStatement;

	/**
	 * The converted statement
	 */
	private String convertedStatement;

	public PredicateInfo(String name, String type, String control, String predicateInitStatement,
			String convertedStatement) {
		super();
		this.name = name;
		this.type = type;
		this.control = control;
		this.predicateInitStatement = predicateInitStatement;
		this.convertedStatement = convertedStatement;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public String getPredicateInitStatement() {
		return predicateInitStatement;
	}

	public void setPredicateInitStatement(String predicateInitStatement) {
		this.predicateInitStatement = predicateInitStatement;
	}

	public String getConvertedStatement() {
		return convertedStatement;
	}

	public void setConvertedStatement(String convertedStatement) {
		this.convertedStatement = convertedStatement;
	}
}
