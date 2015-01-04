package com.karlnosworthy.poijoi;

/**
 * Signals that a data type mapping is unsupported
 * 
 * @author john.bartlett
 */
public class UnsupportedMapping extends Exception {

	private static final long serialVersionUID = 1L;

	public UnsupportedMapping() {
		super();
	}

	public UnsupportedMapping(String message) {
		super(message);
	}

}
