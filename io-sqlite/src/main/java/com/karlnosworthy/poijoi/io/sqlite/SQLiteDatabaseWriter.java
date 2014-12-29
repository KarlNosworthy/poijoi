package com.karlnosworthy.poijoi.io.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.jdbc.JDBCDatabaseCreator;

@SupportsFormat(type = FormatType.SQLITE)
public class SQLiteDatabaseWriter implements Writer {
	
	private static final String COMMAND_OPTION_VERSION = "--version";
	
	
	@Override
	public final void write(String output, PoijoiMetaData metaData, WriteType writeType) throws Exception {
		
		Class.forName("org.sqlite.JDBC");
		Connection connection = DriverManager.getConnection(output);
		
		try {
			JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator();
			
			if (writeType != WriteType.DATA_ONLY) {
				int numberOfTablesCreated = databaseCreator.createTables(connection, metaData);
				System.out.println("Created " + numberOfTablesCreated + " table(s)....");
			}

			if (writeType != WriteType.SCHEMA_ONLY) {
				int inserts = databaseCreator.writeData(connection, metaData);
				System.out.println("Inserted " + inserts + " row(s)....");
			}
		} finally {
			if (connection != null) { 
				connection.close();
			}
		}		
	}
	
/*	
	
	@Override
	protected Connection createConnection(String output) throws Exception {

		// TODO: HOW TO HANDLE OPTIONS...
		Map<String, String> options = new HashMap<String, String>();
		if (options.containsKey(COMMAND_OPTION_VERSION)) {
			Integer versionNumber = Integer.parseInt(options
					.get(COMMAND_OPTION_VERSION));
			setVersionNumber(connection, versionNumber);
		}

		return connection;
	}
*/
	
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
