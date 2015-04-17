package com.karlnosworthy.poijoi.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.UnsupportedMapping;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class JDBCMetaDataReader {

	private static final String TABLE_TYPE_TABLE = "TABLE";

	// getTables result set constants
	private static final String TABLE_RESULT_SCHEMA_NAME_COLUMN_NAME = "TABLE_SCHEM";
	private static final String TABLE_RESULT_TABLE_TYPE_COLUMN_NAME = "TABLE_TYPE";
	private static final String TABLE_RESULT_TABLE_NAME_COLUMN_NAME = "TABLE_NAME";

	// getColumns result set constants
	private static final String COLUMN_RESULT_COLUMN_NAME = "COLUMN_NAME";
	private static final String COLUMN_RESULT_COLUMN_INDEX_NAME = "ORDINAL_POSITION";
	private static final String COLUMN_RESULT_DATA_TYPE_NAME = "DATA_TYPE";

	private DatabaseMetaData databaseMetaData;

	public JDBCMetaDataReader(DatabaseMetaData databaseMetaData) {
		super();
		this.databaseMetaData = databaseMetaData;
	}

	public DatabaseMetaData getDatabaseMetaData() {
		return databaseMetaData;
	}

	public String getDefaultSchemaName() throws SQLException {
		String schemaName = null;
		ResultSet schemaResultSet = null;

		try {
			schemaResultSet = databaseMetaData.getSchemas();
			if (!schemaResultSet.isClosed() && schemaResultSet.next()) {
				schemaName = schemaResultSet.getString(0);
			}
		} finally {
			if (schemaResultSet != null) {
				schemaResultSet.close();
			}
		}
		return schemaName;
	}

	public List<String> getTableNames(String schemaName) throws SQLException {

		List<String> tableNames = new ArrayList<String>();

		ResultSet tablesResultSet = null;

		try {

			tablesResultSet = databaseMetaData.getTables(null, schemaName,
					null, new String[] { TABLE_TYPE_TABLE });

			while (tablesResultSet.next()) {
				if ((schemaName == null || tablesResultSet.getString(
						TABLE_RESULT_SCHEMA_NAME_COLUMN_NAME)
						.equals(schemaName))
						&& tablesResultSet.getString(
								TABLE_RESULT_TABLE_TYPE_COLUMN_NAME).equals(
								TABLE_TYPE_TABLE)
						&& !tablesResultSet.getString(
								TABLE_RESULT_TABLE_NAME_COLUMN_NAME).endsWith(
								"sequence")) {
					tableNames.add(tablesResultSet
							.getString(TABLE_RESULT_TABLE_NAME_COLUMN_NAME));
				}
			}
			tablesResultSet.close();
		} finally {
			if (tablesResultSet != null && !tablesResultSet.isClosed()) {
				tablesResultSet.close();
			}
		}

		return tableNames;
	}

	public Map<String, TableDefinition> getTableDefinitions(
			List<String> tableNames, String schemaName) throws SQLException, UnsupportedMapping {

		Map<String, TableDefinition> tableDefinitions = new HashMap<String, TableDefinition>();

		for (String tableName : tableNames) {

			List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();

			ResultSet tableColumnsResultSet = null;

			try {
				tableColumnsResultSet = databaseMetaData.getColumns(null,
						schemaName, tableName, null);

				while (tableColumnsResultSet.next()) {
					ColumnDefinition columnDefinition = getColumnDefinition(
							tableColumnsResultSet, tableName);

					columnDefinitions.add(columnDefinition);
				}

				tableDefinitions.put(tableName, new TableDefinition(tableName,
						columnDefinitions));

			} finally {
				if (tableColumnsResultSet != null) {
					tableColumnsResultSet.close();
				}
			}
		}

		return tableDefinitions;
	}

	public ColumnDefinition getColumnDefinition(ResultSet tableColumnResetSet,
			String tableName) throws SQLException, UnsupportedMapping {
		String columnName = tableColumnResetSet
				.getString(COLUMN_RESULT_COLUMN_NAME);
		int columnIndex = tableColumnResetSet
				.getInt(COLUMN_RESULT_COLUMN_INDEX_NAME);
		int dataType = tableColumnResetSet.getInt(COLUMN_RESULT_DATA_TYPE_NAME);

		ColumnType columnType = null;
		switch (dataType) {
		case Types.DATE:
			columnType = ColumnType.DATE;
			break;
		case Types.DECIMAL:
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
			columnType = ColumnType.DECIMAL_NUMBER;
			break;
		case Types.INTEGER:
			columnType = ColumnType.INTEGER_NUMBER;
			break;
		case Types.BOOLEAN:
		case Types.CLOB:
		case Types.BLOB:
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.TIME:
		case Types.TIMESTAMP:
			columnType = ColumnType.STRING;
			break;
		default:
			throw new UnsupportedMapping("Unsupported Data Type: " + dataType);
		}
		return new ColumnDefinition(columnName, columnIndex, columnType);
	}
}
