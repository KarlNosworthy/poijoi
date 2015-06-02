package com.karlnosworthy.poijoi.jdbc;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;

import java.sql.PreparedStatement;
import java.util.Map;

public abstract class AbstractStatementCreator<T> {

    public abstract T buildCreateTableStatement(TableDefinition tableDefinition);

    public abstract T buildInsertTableStatement(TableDefinition tableDefinition, Map<String, Object> dataToInsert);

    public abstract T buildCreateIndexStatement(IndexDefinition indexDefinition);



    protected String generateCreateIndexSQL(IndexDefinition indexDefinition) {
        StringBuilder builder = new StringBuilder();

        builder.append("CREATE INDEX ");
        builder.append(indexDefinition.getIndexName());
        builder.append(" ON ");
        builder.append(indexDefinition.getTableName());
        builder.append("(");

        String[] indexColumnNames = indexDefinition.getColumnNames();
        int numOfColumns = indexColumnNames.length;

        for (int indexColumnNumber = 0; indexColumnNumber < numOfColumns; indexColumnNumber++) {
            builder.append(indexColumnNames[indexColumnNumber]);

            if ( (1+indexColumnNumber) < numOfColumns) {
                builder.append(",");
            }
        }

        builder.append(")");

        return builder.toString();
    }

    protected String generateCreateTableSQL(TableDefinition tableDefinition) {

        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE ");
        builder.append(tableDefinition.getTableName());
        builder.append(" (");

        if (!tableDefinition.providesIDColumn()) {
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
