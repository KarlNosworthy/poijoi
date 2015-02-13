package com.karlnosworthy.poijoi.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class JDBCDatabaseCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(JDBCDatabaseCreator.class);
		
	private SQLStatementCreator sqlStatementCreator;
	
	public JDBCDatabaseCreator() {
		this(new SQLStatementCreator());
	}
	
	public JDBCDatabaseCreator(SQLStatementCreator sqlStatementCreator) {
		super();
		this.sqlStatementCreator = sqlStatementCreator;
	}
	
	public boolean create(Connection connection, PoijoiMetaData metaData, WriteType writeType) throws Exception {
		int numberOfTablesCreated = 0;

		Statement statement = null;
		
		try {
			// Create tables
			statement = connection.createStatement();
			Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
			
			for (String tableName : tableDefinitions.keySet()) {
				TableDefinition tableDefinition = tableDefinitions.get(tableName);
				String createTableSQL = sqlStatementCreator.buildCreateTableSQL(tableDefinition);
				statement.execute(createTableSQL);
				numberOfTablesCreated++;
			}
			
			if (writeType != WriteType.SCHEMA_ONLY) {
				int numberOfRowsInserted = 0;
				
				for (String tableName : tableDefinitions.keySet()) {
					List<HashMap<String, Object>> tableData = metaData
							.getTableData(tableName);
					
					if (tableData != null) {
						try {
							statement = connection.createStatement();
							
							List<String> insertSQLStatements = sqlStatementCreator.buildInsertTableSQL(tableName, tableData, tableDefinitions.get(tableName)
																				  .getColumnDefinitions());
							
							for (String sqlString : insertSQLStatements) {
								statement.execute(sqlString);
								numberOfRowsInserted++;
							}
						} finally {
							try {
								statement.close();
							} catch (SQLException sqlException) {
								logger.debug("", sqlException);
							}
						}
					}
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				statement.close();
			} catch (SQLException sqlException) {
				logger.debug("", sqlException);
			}
		}
	}
}
