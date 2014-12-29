package com.karlnosworthy.poijoi.io.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.TableDefinition;

public class JDBCSQLCreator {
	
	/**
	 * 
	 * @param connection
	 * @param tableName
	 * @param columnDefinitions
	 * @return
	 */
	public String buildCreateTableSQL(TableDefinition tableDefinition) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("CREATE TABLE ");
		builder.append(tableDefinition.getTableName());
		builder.append(" (");
		builder.append("id INTEGER PRIMARY KEY AUTOINCREMENT");
		
		for (String columnName : tableDefinition.getColumnDefinitions().keySet()) {
			builder.append(",\"");

			if (columnName.indexOf(".") >= 0) {
				builder.append(columnName.replace('.', '_'));
			} else {
				builder.append(columnName);
			}
			builder.append("\"");
			
			ColumnDefinition columnDefinition = tableDefinition.getColumnDefinition(columnName);

			ColumnType columnType = columnDefinition.getColumnType();

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
	
	
	
	public List<String> buildInsertTableSQL(String tableName, 
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
