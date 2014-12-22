package com.karlnosworthy.poijoi.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoijoiMetaData {

	private boolean readData;
	private Map<String, TableDefinition> tableDefinitions;
	private Map<String, List<HashMap<String, String>>> tableData;

	public PoijoiMetaData(boolean readData,
			Map<String, TableDefinition> tableDefinitions,
			Map<String, List<HashMap<String, String>>> tableData) {
		super();
		this.readData = readData;
		this.tableDefinitions = tableDefinitions;
		this.tableData = tableData;
	}

	public boolean isReadData() {
		return readData;
	}

	public Map<String, TableDefinition> getTableDefinitions() {
		return tableDefinitions;
	}

	public Map<String, List<HashMap<String, String>>> getTableData() {
		return tableData;
	}
	
	public TableDefinition getTableDefinition(String tableName) {
		return tableDefinitions.get(tableName);
	}
	
	public List<HashMap<String, String>> getTableData(String tableName) {
		return tableData.get(tableName);
	}

}
