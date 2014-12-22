package com.karlnosworthy.poijoi.core.io;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.core.SpreadsheetScanner;
import com.karlnosworthy.poijoi.core.SpreadsheetScanner.ColumnType;

public abstract class SpreadsheetReader {
	
	protected File spreadsheetFile;
	protected Map<String, HashMap<String, ColumnType>> tableDefinitions;
	protected Map<String, List<HashMap<String, String>>> tableData;
	
	public SpreadsheetReader(File spreadsheetFile) {
		super();
		this.spreadsheetFile = spreadsheetFile;
		this.tableData = new HashMap<String, List<HashMap<String, String>>>();
		this.tableDefinitions = new HashMap<String, HashMap<String, ColumnType>>();
	}
	
	protected abstract void read();

	public Map<String,HashMap<String,ColumnType>> getTableDefintions() {
		return tableDefinitions;
	}
	
	public Map<String, List<HashMap<String,String>>> getTableData() {
		return tableData;
	}
}
