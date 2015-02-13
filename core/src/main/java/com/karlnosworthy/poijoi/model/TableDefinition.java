package com.karlnosworthy.poijoi.model;

import java.util.Map;

/**
 * Representation of a database table
 * 
 * @author john.bartlett
 *
 */
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

	/**
	 * The number of columns in this table
	 */
	public int getColumnCount() {
		return columnDefinitions.size();
	}

	/**
	 * Get a {@link ColumnDefinition} based on it's name
	 */
	public ColumnDefinition getColumnDefinition(String columnName) {
		return columnDefinitions.get(columnName);
	}
	
	/**
	 * Get a {@link ColumnDefinition} based on it's index
	 */
	public ColumnDefinition getColumnDefinition(int index) {
		for (ColumnDefinition cd : columnDefinitions.values()) {
			if (cd.getColumnIndex() == index) {
				return cd;
			}
		}
		return null;
	}
	
	public boolean isSameAs(TableDefinition tableDefinition) {
		if (tableDefinition != null && tableName != null && tableDefinition.tableName != null && 
			columnDefinitions != null && tableDefinition.columnDefinitions != null &&
			columnDefinitions.size() == tableDefinition.columnDefinitions.size()) {
				
			for (int columnDefinitionIndex = 0; columnDefinitionIndex < columnDefinitions.size(); columnDefinitionIndex++) {
				ColumnDefinition columnDefinition = getColumnDefinition(columnDefinitionIndex);
				if (columnDefinition == null || !columnDefinition.isSameAs(tableDefinition.getColumnDefinition(columnDefinitionIndex))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	

}
