package com.karlnosworthy.poijoi.io.reader;

import java.sql.Connection;

import com.karlnosworthy.poijoi.model.PoijoiMetaData;

public interface JDBCConnectionReader extends Reader<Connection> {
	
	@Override
	public PoijoiMetaData read(Connection connection, boolean readData) throws Exception;
}
