package com.karlnosworthy.poijoi.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;

public class SQLStatementCreator extends AbstractStatementCreator<String> {


	public String buildCreateTableStatement(TableDefinition tableDefinition) {
		return generateCreateTableSQL(tableDefinition);
	}

	public String buildCreateIndexStatement(IndexDefinition indexDefinition) {
		return generateCreateIndexSQL(indexDefinition);
	}


	public String buildInsertTableStatement(TableDefinition tableDefinition, Map<String, Object> dataToImport) {

		StringBuilder builder = new StringBuilder();

		builder.append("INSERT INTO '");
		builder.append(tableDefinition.getTableName());
		builder.append("' (");

		boolean first = true;
		for (String columnName : dataToImport.keySet()) {
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
		for (String columnName : dataToImport.keySet()) {

			columnDefinition = tableDefinition.getColumnDefinition(columnName);

			if (!columnName.endsWith(".id")) {
				if (first) {
					first = false;
				} else {
					builder.append(",");
				}
				Object val = dataToImport.get(columnName);
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
					builder.append(dataToImport.get(columnName));
				}
			}
		}
		builder.append(");");
		return builder.toString();
	}
}
