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
	 * The predicate initialization statement
	 */
	private String initializationStatement;

	/**
	 * The predicate reuse statement. Its used inside the loop control statements.
	 */
	private String reuseStatement;

	/**
	 * The parent statement inside which the predicate is used.
	 */
	private String parentStatement;

	/**
	 * The variable initialization statement
	 */
	private String varInitializationStatement;

	/**
	 * The variable change statement
	 */
	private String varChangeStatement;

	/**
	 * Instantiating the predicate information.
	 * 
	 * @param name
	 * @param initializationStatement
	 * @param reuseStatement
	 * @param parentStatement
	 * @param varInitializationStatement
	 * @param varChangeStatement
	 */
	public PredicateInfo(String name, String initializationStatement, String reuseStatement, String parentStatement,
			String varInitializationStatement, String varChangeStatement) {
		super();
		this.name = name;
		this.initializationStatement = initializationStatement;
		this.reuseStatement = reuseStatement;
		this.parentStatement = parentStatement;
		this.varInitializationStatement = varInitializationStatement;
		this.varChangeStatement = varChangeStatement;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInitializationStatement() {
		return initializationStatement;
	}

	public void setInitializationStatement(String initializationStatement) {
		this.initializationStatement = initializationStatement;
	}

	public String getReuseStatement() {
		return reuseStatement;
	}

	public void setReuseStatement(String reuseStatement) {
		this.reuseStatement = reuseStatement;
	}

	public String getParentStatement() {
		return parentStatement;
	}

	public void setParentStatement(String parentStatement) {
		this.parentStatement = parentStatement;
	}

	public String getVarInitializationStatement() {
		return varInitializationStatement;
	}

	public void setVarInitializationStatement(String varInitializationStatement) {
		this.varInitializationStatement = varInitializationStatement;
	}

	public String getVarChangeStatement() {
		return varChangeStatement;
	}

	public void setVarChangeStatement(String varChangeStatement) {
		this.varChangeStatement = varChangeStatement;
	}
}
