package com.karlnosworthy.poijoi.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;

public abstract class DatabaseCreator {

	public abstract int createTables(PoijoiMetaData metaData);

	public abstract boolean setVersionNumber(Integer versionNumber);

	/**
	 * 
	 * @param tableName
	 * @param columnDefinitions
	 * @return
	 */
	protected String buildCreateTableSQL(String tableName,
			Map<String, ColumnDefinition> columnDefinitions) {
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
