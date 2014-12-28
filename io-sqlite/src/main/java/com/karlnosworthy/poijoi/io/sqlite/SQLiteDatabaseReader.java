package com.karlnosworthy.poijoi.io.sqlite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.JDBCMetaDataReader;
import com.karlnosworthy.poijoi.io.Reader;
import com.karlnosworthy.poijoi.io.SupportsFormat;

@SupportsFormat(type = FormatType.SQLITE)
public class SQLiteDatabaseReader implements Reader {
	
	@Override
	public PoijoiMetaData read(String source, boolean readData) throws Exception {

		PoijoiMetaData metaData = null;
		Connection connection = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			connection = DriverManager.getConnection(source);
			
			Map<String, TableDefinition> tableDefinitions = parseDatabaseMetaData(connection.getMetaData());
			Map<String, List<HashMap<String, String>>> tableData = null;
			
			if (readData) {
				tableData = readData(connection, tableDefinitions);
			}
			metaData = new PoijoiMetaData(readData, tableDefinitions, tableData);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return metaData;
	}
		
	private Map<String, TableDefinition> parseDatabaseMetaData(DatabaseMetaData databaseMetaData) throws SQLException {
		JDBCMetaDataReader metaDataReader = new JDBCMetaDataReader(databaseMetaData);
		
		String schemaName = metaDataReader.getDefaultSchemaName();
		return metaDataReader.getTableDefinitions(metaDataReader.getTableNames(schemaName), schemaName);
	}
	
	private Map<String, List<HashMap<String, String>>> readData(Connection connection, Map<String, TableDefinition> tableDefinitions) throws SQLException {
		
		ResultSet tableDataResultSet = null;
		
		Map<String, List<HashMap<String, String>>> tableData = new HashMap<String, List<HashMap<String, String>>>();
		
		for (String tableName : tableDefinitions.keySet()) {
			
			Statement statement = connection.createStatement();
			tableDataResultSet = statement.executeQuery("select * from " + tableName);
			
			List<HashMap<String, String>> rowData = new ArrayList<HashMap<String, String>>();
			
			ResultSetMetaData resultSetMetaData = tableDataResultSet.getMetaData();
			while (tableDataResultSet.next()) {
				
				HashMap<String, String> columnData = new HashMap<String, String>();
				for (int columnIndex=1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex ++) {
					String columnName = resultSetMetaData.getColumnName(columnIndex);
					String columnStringValue = tableDataResultSet.getString(columnIndex);
					
					columnData.put(columnName, columnStringValue);
				}
				
				rowData.add(columnData);
			}
			
			tableData.put(tableName, rowData);
		}
		return tableData;
	}
}	
