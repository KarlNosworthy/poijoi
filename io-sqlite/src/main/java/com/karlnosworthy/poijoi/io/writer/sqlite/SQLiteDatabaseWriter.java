package com.karlnosworthy.poijoi.io.writer.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.jdbc.JDBCDatabaseCreator;
import com.karlnosworthy.poijoi.io.writer.Writer;

@SupportsFormat(type = FormatType.SQLITE)
public class SQLiteDatabaseWriter implements Writer<Connection> {

	private static final String COMMAND_OPTION_VERSION = "--version";

	@Override
	public final void write(Connection connection, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {
		Class.forName("org.sqlite.JDBC");
		JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator();

		if (writeType != WriteType.DATA_ONLY) {
			int numberOfTablesCreated = databaseCreator.createTables(
					connection, metaData);
			System.out.println("Created " + numberOfTablesCreated
					+ " table(s)....");
		}

		if (writeType != WriteType.SCHEMA_ONLY) {
			int inserts = databaseCreator.writeData(connection, metaData);
			System.out.println("Inserted " + inserts + " row(s)....");
		}
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
