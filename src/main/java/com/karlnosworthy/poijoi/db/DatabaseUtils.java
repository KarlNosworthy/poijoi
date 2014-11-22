package com.karlnosworthy.poijoi.db;

import java.io.File;

public class DatabaseUtils {

	/**
	 * Creates a local JDBC url including the given file path. Defaults to 'sqlite' as the sub-protocol.
	 * 
	 * @param pathToDatabase The path to the database file to create.
	 * @return A string containing the url.
	 */
	public static String createLocalJdbcDatabaseUrl(File pathToDatabase) {
		return createLocalJdbcDatabaseUrl("sqlite", pathToDatabase);
	}

	/**
	/**
	 * Creates a local JDBC url including the given file path. Defaults to 'sqlite' as the sub-protocol.
	 * 
	 * 
	 * @param subProtocol The JDBC sub protocol to use.
	 * @param pathToDatabase The path to the database file to create.
	 * @return A string containing the url.
	 */
	public static String createLocalJdbcDatabaseUrl(String subProtocol, File pathToDatabase) {
		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:");
		builder.append(subProtocol);
		builder.append(":");
		builder.append(pathToDatabase.getAbsolutePath());
		return builder.toString();
	}	
}
