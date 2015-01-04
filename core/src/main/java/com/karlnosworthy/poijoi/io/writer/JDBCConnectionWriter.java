package com.karlnosworthy.poijoi.io.writer;

import java.sql.Connection;

/**
 * Writer that specifically uses a {@link Connection} for it's Destination
 * 
 * @author john.bartlett
 */
public interface JDBCConnectionWriter extends Writer<Connection> {

}
