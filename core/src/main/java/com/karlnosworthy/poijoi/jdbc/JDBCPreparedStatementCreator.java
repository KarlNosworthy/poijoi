package com.karlnosworthy.poijoi.jdbc;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCPreparedStatementCreator extends AbstractStatementCreator<PreparedStatement> {

    private Connection connection;

    public JDBCPreparedStatementCreator(Connection connection) {
        super();
        this.connection = connection;
    }

    /**
     *
     * @param tableDefinition
     * @return
     */
    public PreparedStatement buildCreateTableStatement(TableDefinition tableDefinition) {

        String createTableSQL = generateCreateTableSQL(tableDefinition);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL);
            return preparedStatement;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    public PreparedStatement buildCreateIndexStatement(IndexDefinition indexDefinition) {

        String createIndexSQL = generateCreateIndexSQL(indexDefinition);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(createIndexSQL);
            return preparedStatement;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new prepared statement to insert a row of data into the specified table.
     *
     * @param tableDefinition The table definition to construct the insert statement for.
     * @param connection The connection from which the prepared statement should be obtained.
     * @return The newly created prepared statement or null.
     * @throws SQLException Thrown if an issue occurs while trying to obtain a prepared statement from the connection.
     */
    public PreparedStatement createInsertPreparedStatement(TableDefinition tableDefinition, Connection connection) throws SQLException {
        String[] columnNames = getColumnNames(tableDefinition.getColumnDefinitions());
        String insertSQLWithPlaceholders = generateInsertSQLStringWithPlaceHolders(tableDefinition);

        System.out.println("SQL = "+insertSQLWithPlaceholders);

        return connection.prepareStatement(insertSQLWithPlaceholders, columnNames);
    }

    /**
     *
     * @param preparedStatement
     * @param tableDefinition
     * @param data
     */
    public void populatePreparedStatement(PreparedStatement preparedStatement, TableDefinition tableDefinition, Map<String,Object> data) throws SQLException {

        // check prepared statement is valid
        // check table definition is valid and has column definitions
        // check data is populated

        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {

            int parameterIndex = 1 + columnDefinition.getColumnIndex();

            switch (columnDefinition.getColumnType()) {
                case DATE:
                    java.util.Date date = (java.util.Date) data.get(columnDefinition.getColumnName());
                    preparedStatement.setDate(parameterIndex, new java.sql.Date(date.getTime()));
                    break;
                case DECIMAL_NUMBER:
                    preparedStatement.setDouble(parameterIndex, (Double) data.get(columnDefinition.getColumnName()));
                    break;
                case INTEGER_NUMBER:
                    preparedStatement.setInt(parameterIndex, (Integer) data.get(columnDefinition.getColumnName()));
                    break;
                case STRING:
                    Object value = data.get(columnDefinition.getColumnName());
                    if (value != null) {
                        preparedStatement.setString(parameterIndex, value.toString());
                    } else {
                        preparedStatement.setString(parameterIndex, "");
                    }
                    break;
            }
        }
    }

    /**
     *
     * @param tableDefinition
     * @param dataToInsert
     * @return
     */
    public PreparedStatement buildInsertTableStatement(TableDefinition tableDefinition, Map<String, Object> dataToInsert) {
        // Need to create SQL string with correct placeholders inside it...

        String sqlWithPlaceholders = generateInsertSQLStringWithPlaceHolders(tableDefinition);


        String[] columnNames = getColumnNames(tableDefinition.getColumnDefinitions());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlWithPlaceholders, columnNames);
            return preparedStatement;
        } catch (SQLException sqlException) {
            return null;
        }
    }

    private String generateInsertSQLStringWithPlaceHolders(TableDefinition tableDefinition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ");
        stringBuilder.append(tableDefinition.getTableName());
        stringBuilder.append(" (");

        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
            stringBuilder.append(columnDefinition.getColumnName());
            if (!tableDefinition.isLastColumnDefinition(columnDefinition)) {
                stringBuilder.append(",");
            }
        }

        stringBuilder.append(") VALUES (");

        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
            stringBuilder.append("?");
            if (!tableDefinition.isLastColumnDefinition(columnDefinition)) {
                stringBuilder.append(",");
            }
        }

        stringBuilder.append(");");

        return stringBuilder.toString();
    }

    private String[] getColumnNames(List<ColumnDefinition> columnDefinitions) {
        String[] columnNames = new String[columnDefinitions.size()];

        for (int columnIndex = 0; columnIndex < columnDefinitions.size(); columnIndex ++) {
            ColumnDefinition columnDefinition = columnDefinitions.get(columnIndex);
            columnNames[columnIndex] = columnDefinition.getColumnName();
        }

        return columnNames;
    }

}
