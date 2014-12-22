package com.karlnosworthy.poijoi.io;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;

public interface Reader {

	public enum ReadType {
		DATA, SCHEMA
	}

	/**
	 * Read a source to build a Poijoi meta data object based on the read type
	 * 
	 * @param source
	 * @param readType
	 * @return
	 */
	PoijoiMetaData read(String source, ReadType readType) throws Exception;

}
