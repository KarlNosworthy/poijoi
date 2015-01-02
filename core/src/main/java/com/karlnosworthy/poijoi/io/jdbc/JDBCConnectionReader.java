package com.karlnosworthy.poijoi.io.jdbc;

import java.sql.Connection;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.Reader;

public interface JDBCConnectionReader extends Reader<Connection> {
	
	@Override
	public PoijoiMetaData read(Connection connection, boolean readData) throws Exception;
}
