package org.wltea.analyzer.dic;

public class DictionaryException extends Exception {

	private static final long serialVersionUID = 1L;

	String errorMessage;

	public DictionaryException(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String toString() {
		return errorMessage;
	}

	public String getMessage() {
		return errorMessage;
	}
}
