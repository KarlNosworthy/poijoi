package com.karlnosworthy.poijoi.io;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;

public interface Writer {

	public enum WriteType {
		DATA_ONLY,
		SCHEMA_ONLY,
		BOTH
	}

	void write(String output, PoijoiMetaData metaData, WriteType writeType) throws Exception;

}
