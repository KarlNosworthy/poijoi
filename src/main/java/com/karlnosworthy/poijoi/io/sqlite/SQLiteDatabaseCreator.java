package com.karlnosworthy.poijoi.io.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.sqlite.SQLiteConfig;

public class SQLiteDatabaseCreator extends DatabaseCreator {

	private static final String COMMAND_OPTION_VERSION = "--version";
	
	@Override
	protected Connection createConnection(String output) throws Exception {
		String jdbcUrl = DatabaseUtils.createLocalJdbcDatabaseUrl(new File(
				output));

		Class.forName("org.sqlite.JDBC");
		Connection connection = DriverManager.getConnection(jdbcUrl);

		// TODO: HOW TO HANDLE OPTIONS...
		Map<String, String> options = new HashMap<String, String>();
		if (options.containsKey(COMMAND_OPTION_VERSION)) {
			Integer versionNumber = Integer.parseInt(options
					.get(COMMAND_OPTION_VERSION));
			setVersionNumber(connection, versionNumber);
		}

		return connection;
	}

	private boolean setVersionNumber(Connection connection,
			Integer versionNumber) {
		SQLiteConfig config = new SQLiteConfig();
		config.setUserVersion(versionNumber);
		try {
			config.apply(connection);
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}
}
