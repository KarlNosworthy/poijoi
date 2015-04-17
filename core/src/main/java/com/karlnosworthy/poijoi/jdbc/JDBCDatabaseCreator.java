package com.karlnosworthy.poijoi.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class JDBCDatabaseCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(JDBCDatabaseCreator.class);

	private Connection connection;
	private SQLStatementCreator sqlStatementCreator;
	private JDBCPreparedStatementCreator preparedStatementCreator;
	
	public JDBCDatabaseCreator(Connection connection) {
		super();
		this.connection = connection;
		this.preparedStatementCreator = new JDBCPreparedStatementCreator(connection);
	}

	public JDBCDatabaseCreator(SQLStatementCreator sqlStatementCreator, Connection connection) {
		super();
		this.sqlStatementCreator = sqlStatementCreator;
		this.connection = connection;
	}
	
	public boolean create(PoiJoiMetaData metaData, WriteType writeType) throws Exception {
		int numberOfTablesCreated = 0;

		try {
			// Create tables
			Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
			
			for (String tableName : tableDefinitions.keySet()) {
				TableDefinition tableDefinition = tableDefinitions.get(tableName);

				if (preparedStatementCreator != null) {
					PreparedStatement preparedStatement = preparedStatementCreator.buildCreateTableStatement(tableDefinition);
					if (preparedStatement.execute()) {
						numberOfTablesCreated++;
					}
					preparedStatement.close();
				} else {
					String sqlStatement = sqlStatementCreator.buildCreateTableStatement(tableDefinition);
					Statement statement = connection.createStatement();
					statement.execute(sqlStatement);
					statement.close();
				}
			}
			
			if (writeType != WriteType.SCHEMA_ONLY) {
				int numberOfRowsInserted = 0;

				PreparedStatement preparedStatement = null;

				for (TableDefinition tableDefinition : tableDefinitions.values()) {

					List<HashMap<String, Object>> tableData = metaData
							.getTableData(tableDefinition.getTableName());
					
					if (tableData != null) {

						if (preparedStatementCreator != null) {
							try {
								preparedStatement = preparedStatementCreator.createInsertPreparedStatement(tableDefinition, connection);

								for (int dataToInsertRowIndex = 0; dataToInsertRowIndex < tableData.size(); dataToInsertRowIndex++) {
									Map<String, Object> dataToInsert = tableData.get(dataToInsertRowIndex);

									preparedStatementCreator.populatePreparedStatement(preparedStatement, tableDefinition, dataToInsert);

									if (preparedStatement.execute()) {
										numberOfRowsInserted++;
									}
								}

								preparedStatement.close();

							} finally {
								try {
									if (!preparedStatement.isClosed()) {
										preparedStatement.close();
									}
								} catch (SQLException sqlException) {
									logger.debug("", sqlException);
								}
							}
						} else {

							Statement statement = connection.createStatement();
							for (int dataToInsertRowIndex = 0; dataToInsertRowIndex < tableData.size(); dataToInsertRowIndex++) {
								Map<String, Object> dataToInsert = tableData.get(dataToInsertRowIndex);

								String sql = sqlStatementCreator.buildInsertTableStatement(tableDefinition, dataToInsert);
								if (statement.execute(sql)) {
									numberOfRowsInserted++;
								}
							}
							statement.close();
						}
					}
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
