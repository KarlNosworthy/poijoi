package com.karlnosworthy.poijoi.io.reader;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;

public interface Reader<T> {
	
	/**
	 * Read a source to build a Poijoi meta data object based on the read type
	 * 
	 * @param source
	 * @param readType
	 * @return
	 */
	PoijoiMetaData read(T source, boolean readData) throws Exception;

}
