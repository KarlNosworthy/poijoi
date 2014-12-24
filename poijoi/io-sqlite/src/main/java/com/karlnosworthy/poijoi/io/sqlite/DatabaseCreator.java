package com.karlnosworthy.poijoi.io.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.Writer;

public abstract class DatabaseCreator implements Writer {

	@Override
	public final void write(String output, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {
		Connection connection = createConnection(output);
		try {
			if (writeType != WriteType.DATA_ONLY) {
				int numberOfTablesCreated = createTables(connection, metaData);
				System.out.println("Created " + numberOfTablesCreated
						+ " table(s)....");
			}

			if (writeType != WriteType.SCHEMA_ONLY) {
				int inserts = writeData(connection, metaData);
				System.out.println("Inserted " + inserts + " row(s)....");
			}
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	protected abstract Connection createConnection(String output)
			throws Exception;

	protected int createTables(Connection connection, PoijoiMetaData metaData) {
		int numberOfTablesCreated = 0;

		Statement statement = null;
		try {
			statement = connection.createStatement();
			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			for (String tableName : tableDefinitions.keySet()) {
				String sql = buildCreateTableSQL(connection, tableName,
						tableDefinitions.get(tableName).getColumnDefinitions());
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

	protected int writeData(Connection connection, PoijoiMetaData metaData)
			throws SQLException {
		int numberOfRowsInserted = 0;
		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			List<HashMap<String, String>> tableData = metaData
					.getTableData(tableName);
			if (tableData != null) {
				Statement statement = null;
				try {
					statement = connection.createStatement();
					List<String> sqlStrings = buildInsertTableSQL(tableName,
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

	/**
	 * 
	 * @param connection
	 * @param tableName
	 * @param columnDefinitions
	 * @return
	 */
	protected String buildCreateTableSQL(Connection connection,
			String tableName, Map<String, ColumnDefinition> columnDefinitions) {
		StringBuilder builder = new StringBuilder();

		builder.append("CREATE TABLE ");
		builder.append(tableName);
		builder.append(" (");
		builder.append("id INTEGER PRIMARY KEY AUTOINCREMENT");

		for (String columnName : columnDefinitions.keySet()) {
			builder.append(",\"");

			if (columnName.indexOf(".") >= 0) {
				builder.append(columnName.replace('.', '_'));
			} else {
				builder.append(columnName);
			}
			builder.append("\"");

			ColumnType columnType = columnDefinitions.get(columnName)
					.getColumnType();

			switch (columnType) {
			case STRING:
				builder.append(" TEXT");
				break;
			case INTEGER_NUMBER:
				builder.append(" INTEGER");
				break;
			case DECIMAL_NUMBER:
				builder.append(" REAL");
			case DATE:
				break;
			}
		}
		builder.append(");");

		return builder.toString();
	}

	protected List<String> buildInsertTableSQL(String tableName,
			List<HashMap<String, String>> dataToImport,
			Map<String, ColumnDefinition> columnDefinitions) {
		List<String> insertSqlStrings = new ArrayList<String>();

		StringBuilder builder = null;
		for (HashMap<String, String> columnData : dataToImport) {
			builder = new StringBuilder();

			builder.append("INSERT INTO '");
			builder.append(tableName);
			builder.append("' (");

			boolean first = true;
			for (String columnName : columnData.keySet()) {
				if (!columnName.endsWith(".id")) {
					if (first) {
						first = false;
					} else {
						builder.append(",");
					}
					builder.append("\"");
					builder.append(columnName);
					builder.append("\"");
				}
			}

			builder.append(")");
			builder.append(" VALUES (");

			first = true;
			for (String columnName : columnData.keySet()) {
				if (!columnName.endsWith(".id")) {
					if (first) {
						first = false;
					} else {
						builder.append(",");
					}
					ColumnType columnType = columnDefinitions.get(columnName)
							.getColumnType();
					if (columnType == ColumnType.STRING
							|| columnType == ColumnType.DATE) {
						builder.append("'");
						builder.append(columnData.get(columnName));
						builder.append("'");
					} else {
						builder.append(columnData.get(columnName));
					}

				}
			}

			builder.append(");");

			insertSqlStrings.add(builder.toString());
		}
		return insertSqlStrings;
	}
}
