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

}
