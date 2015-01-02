package com.karlnosworthy.poijoi.model;

import java.util.Map;

public class TableDefinition {

	private String tableName;

	private Map<String, ColumnDefinition> columnDefinitions;

	public TableDefinition(String tableName,
			Map<String, ColumnDefinition> columnDefinitions) {
		super();
		this.tableName = tableName;
		this.columnDefinitions = columnDefinitions;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}

	public ColumnDefinition getColumnDefinition(String columnName) {
		return columnDefinitions.get(columnName);
	}

	public int getColumnCount() {
		return columnDefinitions.size();
	}

	public ColumnDefinition getColumnDefinition(int index) {
		for (ColumnDefinition cd : columnDefinitions.values()) {
			if (cd.getColumnIndex() == index) {
				return cd;
			}
		}
		return null;
	}

}
