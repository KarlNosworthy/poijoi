package com.karlnosworthy.poijoi.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.io.Reader.ReadType;

public class PoijoiMetaData {

	private ReadType readType;
	private Map<String, TableDefinition> tableDefinitions;
	private Map<String, List<HashMap<String, String>>> tableData;

	public PoijoiMetaData(ReadType readType,
			Map<String, TableDefinition> tableDefinitions,
			Map<String, List<HashMap<String, String>>> tableData) {
		super();
		this.readType = readType;
		this.tableDefinitions = tableDefinitions;
		this.tableData = tableData;
	}

	public ReadType getReadType() {
		return readType;
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
