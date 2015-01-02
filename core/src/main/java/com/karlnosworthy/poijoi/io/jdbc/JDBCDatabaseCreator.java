package com.karlnosworthy.poijoi.io.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;

public class JDBCDatabaseCreator {
	
	private JDBCSQLCreator sqlCreator;
	
	public JDBCDatabaseCreator() {
		super();
		this.sqlCreator = new JDBCSQLCreator();
	}

	public int createTables(Connection connection, PoijoiMetaData metaData) {
		int numberOfTablesCreated = 0;

		Statement statement = null;
		try {
			statement = connection.createStatement();
			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			
			for (String tableName : tableDefinitions.keySet()) {
				TableDefinition tableDefinition = tableDefinitions.get(tableName);
				String sql = sqlCreator.buildCreateTableSQL(tableDefinition);
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

	public int writeData(Connection connection, PoijoiMetaData metaData)
			throws SQLException {
		
		int numberOfRowsInserted = 0;
		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			List<HashMap<String, Object>> tableData = metaData
					.getTableData(tableName);
			if (tableData != null) {
				Statement statement = null;
				try {
					statement = connection.createStatement();
					List<String> sqlStrings = sqlCreator.buildInsertTableSQL(tableName,
							tableData, tableDefinitions.get(tableName)
									.getColumnDefinitions());
					for (String sqlString : sqlStrings) {
						statement.execute(sqlString);
						numberOfRowsInserted++;
					}
				} finally {
					try {
						statement.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		return numberOfRowsInserted;
	}
}
