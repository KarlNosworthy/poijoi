package com.karlnosworthy.poijoi.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.karlnosworthy.poijoi.PoiJoiConfiguration;
import com.karlnosworthy.poijoi.core.io.SpreadsheetReader;
import com.karlnosworthy.poijoi.core.io.ods.ODSSpreadsheetReader;
import com.karlnosworthy.poijoi.core.io.xls.XLSSpreadsheetReader;

public class SpreadsheetScanner {

	public enum ColumnType {
		STRING,
		DATE,
		INTEGER_NUMBER,
		DECIMAL_NUMBER
	}
	
	private SpreadsheetReader reader;
	
	
	public SpreadsheetScanner(File inputFile) throws IOException {
		super();
		this.reader = findReaderForFile(inputFile);
	}
	
	public Map<String,HashMap<String,ColumnType>> getTableDefinitions() {
		return reader.getTableDefintions();
	}
	
	public Set<String>getTableNames() {
		return reader.getTableDefintions().keySet();
	}
	
	public Set<String>getColumnNames(String tableName) {
		return reader.getTableDefintions().get(tableName).keySet();
	}
	
	public Map<String,ColumnType>getColumnNamesAndTypes(String tableName) {
		return reader.getTableDefintions().get(tableName);
	}
	
	public List<HashMap<String,String>> getTableData(String tableName) {
		return reader.getTableData().get(tableName);
	}
	
	private SpreadsheetReader findReaderForFile(File inputFile) {
		SpreadsheetReader spreadsheetReader = null;
		
		String inputFilePath = inputFile.getAbsolutePath();
		if (inputFilePath.endsWith(PoiJoiConfiguration.OPEN_OFFICE_DOCUMENT_EXTENSION)) {
			spreadsheetReader = new ODSSpreadsheetReader(inputFile);
		} else if (inputFilePath.endsWith(PoiJoiConfiguration.MS_EXCEL_DOCUMENT_EXTENSION)) {
			spreadsheetReader = new XLSSpreadsheetReader(inputFile);
		}
		
		return spreadsheetReader;
	}
}
