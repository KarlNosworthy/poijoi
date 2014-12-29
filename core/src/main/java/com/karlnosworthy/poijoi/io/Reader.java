package com.karlnosworthy.poijoi.io;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;

public interface Reader {
	
	/**
	 * Read a source to build a Poijoi meta data object based on the read type
	 * 
	 * @param source
	 * @param readType
	 * @return
	 */
	PoijoiMetaData read(String source, boolean readData) throws Exception;

}
