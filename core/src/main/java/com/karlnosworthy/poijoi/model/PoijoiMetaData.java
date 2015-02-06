package com.karlnosworthy.poijoi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;

/**
 * Storage for the data being passed between the {@link Reader} and
 * {@link Writer}.
 * 
 * Contains the database structure and the underlying database data (if
 * requested)
 * 
 * @author john.bartlett
 *
 */
public class PoijoiMetaData {

	private boolean readData;
	private Map<String, TableDefinition> tableDefinitions;
	private Map<String, List<HashMap<String, Object>>> tableData;

	public PoijoiMetaData(boolean readData,
			Map<String, TableDefinition> tableDefinitions,
			Map<String, List<HashMap<String, Object>>> tableData) {
		super();
		this.readData = readData;
		this.tableDefinitions = tableDefinitions;
		this.tableData = tableData;
	}

	/**
	 * Was the database data read
	 */
	public boolean isReadData() {
		return readData;
	}
	
	public boolean isEmpty() {
		if (tableDefinitions == null || tableDefinitions.isEmpty()) {
			return true;
		} else if (readData && (tableData == null || tableData.isEmpty())) {
			return true;
		}
		
		return false;
	}

	public Map<String, TableDefinition> getTableDefinitions() {
		return tableDefinitions;
	}

	public Map<String, List<HashMap<String, Object>>> getTableData() {
		return tableData;
	}

	/**
	 * Get the {@link TableDefinition} based on it's name
	 * 
	 * @param tableName
	 *            The Database Table name
	 */
	public TableDefinition getTableDefinition(String tableName) {
		return tableDefinitions.get(tableName);
	}

	/**
	 * Get the data for a given database table
	 * 
	 * @param tableName
	 *            The table to get the data for
	 * @return List<HashMap<String, Object>> where each HashMap represents a row
	 *         and the String key is the column name
	 */
	public List<HashMap<String, Object>> getTableData(String tableName) {
		return tableData.get(tableName);
	}

	public boolean isSameAs(PoijoiMetaData metaData) {
		
		if (metaData != null) {
			
			if (readData == metaData.readData) {
				
				if (tableDefinitions != null && metaData.tableDefinitions != null &&
					tableDefinitions.size() == metaData.tableDefinitions.size()) {
					
					for (String tableName : tableDefinitions.keySet()) {
						
						TableDefinition tableDefinition = tableDefinitions.get(tableName);
						TableDefinition tableDefinitionToCompare = metaData.tableDefinitions.get(tableName);
						
						if (tableDefinition != null && tableDefinitionToCompare != null) {
							if (!tableDefinition.isSameAs(tableDefinitionToCompare)) {
								return false;
							}
						}
					}
				}
				
				if (tableData == metaData.tableData) {
					if (tableData != null && metaData.tableData != null &&
						tableData.size() == metaData.tableData.size()) {
						
						for (String tableName : tableDefinitions.keySet()) {
							List<HashMap<String, Object>> tableDataRows = tableData.get(tableName);
							List<HashMap<String, Object>> tableDataRowsToCompare = metaData.tableData.get(tableName);
							
							if (tableDataRows != null && tableDataRowsToCompare != null &&
								tableDataRows.size() == tableDataRowsToCompare.size()) {
								
								for (int tableRowIndex = 0; tableRowIndex < tableDataRows.size(); tableRowIndex ++) {
								
									HashMap<String, Object> tableRowData = tableDataRows.get(tableRowIndex);
									HashMap<String, Object> tableRowDataToCompare = tableDataRowsToCompare.get(tableRowIndex);
									
									if (tableRowData != null && tableRowDataToCompare != null &&
										tableRowData.size() == tableRowDataToCompare.size()) {
										
										for (String columnName : tableRowData.keySet()) {
											
											Object tableColumnData = tableRowData.get(columnName);
											Object tableColumnDataToCompare = tableRowDataToCompare.get(columnName);
											
											if (tableColumnData != null && tableColumnDataToCompare != null) {
												if (!tableColumnData.equals(tableColumnDataToCompare)) {
													return false;
												}
											}
										}
									}
								}
							}
							return true;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
}
