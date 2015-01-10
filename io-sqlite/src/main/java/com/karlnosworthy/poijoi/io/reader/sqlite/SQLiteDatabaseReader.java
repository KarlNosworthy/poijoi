package com.karlnosworthy.poijoi.io.reader.sqlite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.UnsupportedMapping;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.JDBCConnectionReader;
import com.karlnosworthy.poijoi.jdbc.JDBCMetaDataReader;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Read an SQLite Database and populate the content into a
 * {@link PoijoiMetaData}.
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = "SQLITE")
public class SQLiteDatabaseReader implements JDBCConnectionReader {

	@Override
	public boolean isValidConnection(Connection connection) {
		
		if (connection == null) {
			return false;
		} else {
			try {
				if (connection.isClosed()) {
					return false;
				}
			} catch (SQLException sqlException) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Builds a {@link PoijoiMetaData} object representing an SQLite Database
	 * structure and optionally the data itself using a {@link Connection}.
	 * 
	 * @param connection
	 *            The connection to the SQLite Database
	 * @param readData
	 *            Whether or not to read the data or just the database structure
	 * @return a {@link PoijoiMetaData} holding the table structures and
	 *         optionally the table data
	 */
	@Override
	public PoijoiMetaData read(Connection connection, boolean readData)
			throws Exception {
		
		if (isValidConnection(connection)) {
			
			Class.forName("org.sqlite.JDBC");
			Map<String, TableDefinition> tableDefinitions = parseDatabaseMetaData(connection
					.getMetaData());
			Map<String, List<HashMap<String, Object>>> tableData = null;
			if (readData) {
				tableData = readData(connection, tableDefinitions);
			}
			return new PoijoiMetaData(readData, tableDefinitions, tableData);
		} else {
			return null;
		}
	}

	private Map<String, TableDefinition> parseDatabaseMetaData(
			DatabaseMetaData databaseMetaData) throws SQLException,
			UnsupportedMapping {
		JDBCMetaDataReader metaDataReader = new JDBCMetaDataReader(
				databaseMetaData);
		String schemaName = metaDataReader.getDefaultSchemaName();
		return metaDataReader.getTableDefinitions(
				metaDataReader.getTableNames(schemaName), schemaName);
	}

	private Map<String, List<HashMap<String, Object>>> readData(
			Connection connection, Map<String, TableDefinition> tableDefinitions)
			throws SQLException, UnsupportedMapping {

		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		for (String tableName : tableDefinitions.keySet()) {

			Statement statement = connection.createStatement();
			ResultSet tableDataResultSet = statement
					.executeQuery("select * from " + tableName);

			List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
			ResultSetMetaData resultSetMetaData = tableDataResultSet
					.getMetaData();
			while (tableDataResultSet.next()) {

				HashMap<String, Object> columnData = new HashMap<String, Object>();
				for (int columnIndex = 1; columnIndex <= resultSetMetaData
						.getColumnCount(); columnIndex++) {
					String columnName = resultSetMetaData
							.getColumnName(columnIndex);
					int columnType = resultSetMetaData
							.getColumnType(columnIndex);
					Object value = null;
					switch (columnType) {
					case Types.DATE:
						value = tableDataResultSet.getDate(columnIndex);
						break;
					case Types.DECIMAL:
					case Types.REAL:
						value = tableDataResultSet.getDouble(columnIndex);
						break;
					case Types.INTEGER:
						value = tableDataResultSet.getInt(columnIndex);
						break;
					case Types.BOOLEAN:
					case Types.CLOB:
					case Types.BLOB:
					case Types.CHAR:
					case Types.VARCHAR:
						value = tableDataResultSet.getString(columnIndex);
						break;
					default:
						throw new UnsupportedMapping("Unsupported Data Type: "
								+ columnType);
					}
					columnData.put(columnName, value);
				}
				rowData.add(columnData);
			}
			tableData.put(tableName, rowData);
		}
		return tableData;
	}
}
