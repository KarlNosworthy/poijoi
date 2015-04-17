package com.karlnosworthy.poijoi;

import java.util.HashMap;
import java.util.Map;

public class PoiJoiOptions {
	
	public static final String OPTION_INFO = "--info";
	public static final String OPTION_COMPARE = "--compare";
	
	private Map<String,String> options;
	
	public PoiJoiOptions() {
		super();
		this.options = new HashMap<String,String>();
	}
	
	public PoiJoiOptions(Map<String,String> options) {
		super();
		this.options = options;
	}
	
	public boolean hasValue(String optionName) {
		return options.containsKey(optionName);
	}
	
	public String getValue(String optionName) {
		if (options.containsKey(optionName)) {
			return options.get(optionName);
		}
		return null;
	}
	
}
