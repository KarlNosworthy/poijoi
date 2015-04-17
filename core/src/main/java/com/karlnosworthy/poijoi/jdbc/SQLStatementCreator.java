package com.karlnosworthy.poijoi.jdbc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;

public class SQLStatementCreator {
	
	public String buildCreateTableSQL(TableDefinition tableDefinition) {

		StringBuilder builder = new StringBuilder();

		builder.append("CREATE TABLE ");
		builder.append(tableDefinition.getTableName());
		builder.append(" (");
		builder.append("id INTEGER PRIMARY KEY AUTOINCREMENT");

		for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {

			String columnName = columnDefinition.getColumnName();
			ColumnType columnType = columnDefinition.getColumnType();

			builder.append(",\"");

			if (columnName.indexOf(".") >= 0) {
				builder.append(columnName.replace('.', '_'));
			} else {
				builder.append(columnName);
			}
			builder.append("\"");

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
	
	public List<String> buildInsertTableSQL(TableDefinition tableDefinition,
			List<HashMap<String, Object>> dataToImport) {

		List<String> insertSqlStrings = new ArrayList<String>();

		StringBuilder builder = null;

		for (HashMap<String, Object> columnData : dataToImport) {
			builder = new StringBuilder();

			builder.append("INSERT INTO '");
			builder.append(tableDefinition.getTableName());
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

			ColumnDefinition columnDefinition = null;

			first = true;
			for (String columnName : columnData.keySet()) {

				columnDefinition = tableDefinition.getColumnDefinition(columnName);

				if (!columnName.endsWith(".id")) {
					if (first) {
						first = false;
					} else {
						builder.append(",");
					}
					Object val = columnData.get(columnName);
					if (columnDefinition.getColumnType() == ColumnType.DATE) {
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						builder.append("'");
						builder.append(dateFormat.format(val));
						builder.append("'");
					} else if (columnDefinition.getColumnType() == ColumnType.STRING) {
						builder.append("'");
						builder.append(val.toString().replace("'", "''"));
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
