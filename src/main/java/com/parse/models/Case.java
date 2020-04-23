package com.parse.models;

import java.util.List;

/**
 * The model Case. It holds information of a switch case.
 */
public class Case {

	/**
	 * The operand
	 */
	private String operand;

	/**
	 * The body
	 */
	private List<String> body;

	/**
	 * If the switch is present with break
	 */
	private boolean withBreak;

	public Case(String operand, List<String> body, boolean withBreak) {
		super();
		this.operand = operand;
		this.body = body;
		this.withBreak = withBreak;
	}

	public String getOperand() {
		return operand;
	}

	public void setOperand(String operand) {
		this.operand = operand;
	}

	public List<String> getBody() {
		return body;
	}

	public void setBody(List<String> body) {
		this.body = body;
	}

	public boolean isWithBreak() {
		return withBreak;
	}

	public void setWithBreak(boolean withBreak) {
		this.withBreak = withBreak;
	}
}
