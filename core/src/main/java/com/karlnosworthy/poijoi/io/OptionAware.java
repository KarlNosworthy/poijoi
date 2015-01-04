package com.karlnosworthy.poijoi.io;

import com.karlnosworthy.poijoi.PoiJoiOptions;

/**
 *  An object which is aware of options and can act on them
 */
public interface OptionAware {
	public void setOptions(PoiJoiOptions options);
}
