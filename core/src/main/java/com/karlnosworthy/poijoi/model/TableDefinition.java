package com.karlnosworthy.poijoi.model;

import java.util.List;
import java.util.Map;

/**
 * Representation of a database table
 * 
 * @author john.bartlett
 *
 */
public class TableDefinition {

	private String tableName;
	private List<ColumnDefinition> columnDefinitions;
	private List<IndexDefinition> indexDefinitions;


	public TableDefinition(String tableName, List<ColumnDefinition> columnDefinitions) {
		this(tableName, columnDefinitions, null);
	}

	public TableDefinition(String tableName, List<ColumnDefinition> columnDefinitions, List<IndexDefinition> indexDefinitions) {
		super();
		this.tableName = tableName;
		this.columnDefinitions = columnDefinitions;
		this.indexDefinitions = indexDefinitions;
	}


	public String getTableName() {
		return tableName;
	}

	public List<ColumnDefinition> getColumnDefinitions() {
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
		for (ColumnDefinition cd : columnDefinitions) {
			if (cd.getColumnName().equals(columnName)) {
				return cd;
			}
		}
		return null;
	}
	
	/**
	 * Get a {@link ColumnDefinition} based on it's index
	 */
	public ColumnDefinition getColumnDefinition(int index) {
		for (ColumnDefinition cd : columnDefinitions) {
			if (cd.getColumnIndex() == index) {
				return cd;
			}
		}
		return null;
	}

	public boolean containsDefinitionForColumn(String columnName) {
		for (ColumnDefinition cd : columnDefinitions) {
			if (cd.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isLastColumnDefinition(ColumnDefinition columnDefinition) {
		if (columnDefinition != null && !columnDefinitions.isEmpty()) {
			int index = columnDefinitions.indexOf(columnDefinition);
			if (index > 0 && index == (columnDefinitions.size() - 1)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasIndexDefinitions() {
		if (indexDefinitions !=null && !indexDefinitions.isEmpty()) {
			return true;
		}
		return false;
	}

	public List<IndexDefinition> getIndexDefinitions() {
		return indexDefinitions;
	}

	public IndexDefinition getIndexDefinition(int index) {
		if (index >= 0 && index < indexDefinitions.size()) {
			return indexDefinitions.get(index);
		}
		return null;
	}

	public boolean providesIDColumn() {
		if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
			for (ColumnDefinition columnDefinition : columnDefinitions) {
				if (columnDefinition.isIDColumn()) {
					return true;
				}
			}
		}
		return false;
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
