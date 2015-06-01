package com.karlnosworthy.poijoi.model;

public class IndexDefinition {

    private String indexName;
    private String tableName;
    private String[] columnNames;
    private boolean unique;


    public IndexDefinition(String indexName, String tableName, String[] columnNames, boolean unique) {
        super();
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.unique = unique;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public boolean isUnique() {
        return unique;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public String getColumnName(int columnIndex) {
        if (columnNames != null && columnNames.length > 0 && columnIndex >=0 && columnIndex < columnNames.length) {
            return columnNames[columnIndex];
        }
        return null;
    }

    public boolean isComposite() {
        if (columnNames != null && columnNames.length > 1) {
            return true;
        }
        return false;
    }
}
