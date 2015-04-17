package com.karlnosworthy.poijoi.jdbc;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;

import java.util.Map;

public abstract class AbstractStatementCreator<T> {

    public abstract T buildCreateTableStatement(TableDefinition tableDefinition);

    public abstract T buildInsertTableStatement(TableDefinition tableDefinition, Map<String, Object> dataToInsert);


    protected String generateCreateTableSQL(TableDefinition tableDefinition) {

        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE ");
        builder.append(tableDefinition.getTableName());
        builder.append(" (");

        if (!tableDefinition.containsDefinitionForColumn("id")) {
            builder.append("id INTEGER PRIMARY KEY AUTOINCREMENT");
            builder.append(",");
        }

        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {

            String columnName = columnDefinition.getColumnName();
            ColumnDefinition.ColumnType columnType = columnDefinition.getColumnType();

            builder.append("\"");

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

            if (!tableDefinition.isLastColumnDefinition(columnDefinition)) {
                builder.append(",");
            }
        }
        builder.append(");");

        return builder.toString();
    }
}
