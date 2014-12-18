package com.karlnosworthy.poijoi.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class SpreadsheetScanner {

	public enum ColumnType {
		STRING,
		DATE,
		INTEGER_NUMBER,
		DECIMAL_NUMBER
	}
	
	private File inputFile;
	private Map<String,HashMap<String,ColumnType>> tableDefinitions;
	private Map<String, List<HashMap<String,String>>> tableData;
	
	
	
	public SpreadsheetScanner(File inputFile) throws IOException {
		super();
		this.inputFile = inputFile;
		this.tableDefinitions = new HashMap<String,HashMap<String,ColumnType>>();
		this.tableData = new HashMap<String, List<HashMap<String,String>>>();
		
		read();
	}
	
	public Map<String,HashMap<String,ColumnType>> getTableDefinitions() {
		return tableDefinitions;
	}
	
	public Set<String>getTableNames() {
		return tableDefinitions.keySet();
	}
	
	public Set<String>getColumnNames(String tableName) {
		return tableDefinitions.get(tableName).keySet();
	}
	
	public Map<String,ColumnType>getColumnNamesAndTypes(String tableName) {
		return tableDefinitions.get(tableName);
	}
	
	public List<HashMap<String,String>> getTableData(String tableName) {
		return tableData.get(tableName);
	}
	
	private void read() {
		InputStream ios = null;
		
		try {
			ios = new FileInputStream(inputFile);
			HSSFWorkbook workbook = new HSSFWorkbook(ios);
			
			// extract table names....
			List<String> tableNames = new ArrayList<String>();
			
			List<HashMap<String,String>> rowData = null;
			
			int totalNumberOfSheets = workbook.getNumberOfSheets();
			for (int sheetIndex = 0; sheetIndex < totalNumberOfSheets; sheetIndex++) {
				HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				// Find columns...
				HSSFRow headerRow = sheet.getRow(0);
				//If we don't have any columns then there's nothing we can do
				if (headerRow != null) {
					String tableName = sheet.getSheetName();
					tableNames.add(tableName);
					
					HSSFRow typedRow = sheet.getRow(1);
				
					List<String> columnNames = new ArrayList<String>();
					
					HashMap<String,ColumnType> columnNamesAndTypes = new HashMap<String,ColumnType>();
					
					DataFormatter dataFormatter = new DataFormatter();
					
					for (int cellIndex = headerRow.getFirstCellNum(); cellIndex < headerRow.getLastCellNum(); cellIndex++) {
						HSSFCell headerRowCell = headerRow.getCell(cellIndex);
						String cellName = headerRowCell.getStringCellValue();
						
						columnNames.add(cellName);
						
						HSSFCell typedRowCell = null;
						if (typedRow != null) {
							typedRowCell = typedRow.getCell(cellIndex);
						}
						
						ColumnType columnType = ColumnType.STRING;
						
						int cellType = Cell.CELL_TYPE_BLANK;
						if (typedRowCell != null) {
							cellType = typedRowCell.getCellType();
						}
						
						switch (cellType) {
							case Cell.CELL_TYPE_BLANK:
								if (cellName.endsWith(".id")) {
									columnType = ColumnType.INTEGER_NUMBER;
								} else {
									columnType = ColumnType.STRING;
								}
								break;
							case Cell.CELL_TYPE_BOOLEAN:
							case Cell.CELL_TYPE_ERROR:
							case Cell.CELL_TYPE_STRING:
								columnType = ColumnType.STRING;
								break;
							case Cell.CELL_TYPE_FORMULA:
							case Cell.CELL_TYPE_NUMERIC:
								if (HSSFDateUtil.isCellDateFormatted(typedRowCell)) {
									columnType = ColumnType.DATE;
								} else {
									String formattedValue = dataFormatter.formatCellValue(typedRowCell);
									
									if (formattedValue.contains(".")) {
										columnType = ColumnType.DECIMAL_NUMBER;
									} else {
										columnType = ColumnType.INTEGER_NUMBER;
									}
								}
								break;
						}
						
						columnNamesAndTypes.put(headerRowCell.getStringCellValue(),
								columnType);
					}
					
					this.tableDefinitions.put(tableName, columnNamesAndTypes);
					
					rowData = new ArrayList<HashMap<String,String>>();
					
					HashMap<String,String> columnData = new HashMap<String,String>();
					if (sheet.getLastRowNum() > 1) {
						for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
							HSSFRow dataRow = sheet.getRow(rowIndex);
							
							columnData = new HashMap<String,String>();
							for (int cellIndex = dataRow.getFirstCellNum(); cellIndex <= (dataRow.getLastCellNum() - 1); cellIndex++) {
								String colName = columnNames.get(cellIndex);
								HSSFCell dataCell = dataRow.getCell(cellIndex);
								
								if (dataCell.getCellType() == Cell.CELL_TYPE_STRING) {
									columnData.put(colName,dataCell.getStringCellValue().trim());
								} else if (HSSFDateUtil.isCellDateFormatted(dataCell)) {
									// Default handling of date is to convert to string (if sqlite)
									// may need to implement 'per database column type mapping'
									// convert to ISO8601 string = YYYY-MM-DD HH:MM:SS.SSS
									SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
									Date date = dataCell.getDateCellValue();
									
									String dateFormattedCellValue = "";
									if (date != null) {
										dateFormattedCellValue = dateFormat.format(date);
									} else {
										dateFormattedCellValue = dataCell.getStringCellValue();
									}
									
									columnData.put(colName,dateFormattedCellValue);
								} else {
									if (columnNamesAndTypes.get(colName) == ColumnType.INTEGER_NUMBER) {
										columnData.put(colName, String.valueOf(new Double(dataCell.getNumericCellValue()).intValue()).trim());
									} else {
										String formattedDecimalValue = dataFormatter.formatCellValue(dataCell);
										columnData.put(colName, formattedDecimalValue);
									}
								}
							}
							
							rowData.add(columnData);
						}
						this.tableData.put(tableName, rowData);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ios != null) {
				try {
					ios.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
