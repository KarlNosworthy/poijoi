package com.karlnosworthy.poijoi.jdbc;

import java.text.SimpleDateFormat;
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

		for (String columnName : tableDefinition.getColumnDefinitions()
				.keySet()) {
			builder.append(",\"");

			if (columnName.indexOf(".") >= 0) {
				builder.append(columnName.replace('.', '_'));
			} else {
				builder.append(columnName);
			}
			builder.append("\"");

			ColumnDefinition columnDefinition = tableDefinition
					.getColumnDefinition(columnName);

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
				break;
			case DATE:
				builder.append(" DATE");
				break;
			}
		}
		builder.append(");");

		return builder.toString();
	}

	public List<String> buildInsertTableSQL(String tableName,
			List<HashMap<String, Object>> dataToImport,
			Map<String, ColumnDefinition> columnDefinitions) {

		List<String> insertSqlStrings = new ArrayList<String>();

		StringBuilder builder = null;
		for (HashMap<String, Object> columnData : dataToImport) {
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
					Object val = columnData.get(columnName);
					if (columnType == ColumnType.DATE) {
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						builder.append("'");
						builder.append(dateFormat.format(val));
						builder.append("'");
					} else if (columnType == ColumnType.STRING) {
						builder.append("'");
						builder.append(val);
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
