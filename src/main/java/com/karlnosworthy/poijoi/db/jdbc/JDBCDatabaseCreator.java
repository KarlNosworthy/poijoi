package com.karlnosworthy.poijoi.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sqlite.SQLiteConfig;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.db.DatabaseCreator;

public class JDBCDatabaseCreator extends DatabaseCreator {

	private Connection connection;

	public JDBCDatabaseCreator(Connection connection) {
		super();
		this.connection = connection;
	}

	public boolean setVersionNumber(Integer versionNumber) {
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

	public int createTables(PoijoiMetaData metaData) {
		int numberOfTablesCreated = 0;

		Statement statement = null;
		try {
			statement = connection.createStatement();
			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			for (String tableName : tableDefinitions.keySet()) {
				String sql = buildCreateTableSQL(tableName, tableDefinitions
						.get(tableName).getColumnDefinitions());
				statement.execute(sql);
				numberOfTablesCreated++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
		return numberOfTablesCreated;
	}

	public int insertRowsIntoTable(String tableName,
			List<HashMap<String, String>> dataToImport,
			Map<String, ColumnDefinition> columnDefinitions) {
		int numberOfRowsInserted = 0;

		Statement statement = null;
		try {
			statement = connection.createStatement();

			List<String> sqlStrings = buildInsertTableSQL(tableName,
					dataToImport, columnDefinitions);

			for (String sqlString : sqlStrings) {
				statement.execute(sqlString);
				numberOfRowsInserted++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
		return numberOfRowsInserted;
	}
}
