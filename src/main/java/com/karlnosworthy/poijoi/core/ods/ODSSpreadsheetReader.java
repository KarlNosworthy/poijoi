package com.karlnosworthy.poijoi.core.ods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.core.SpreadsheetReader;
import com.karlnosworthy.poijoi.core.SpreadsheetScanner.ColumnType;

public final class ODSSpreadsheetReader extends SpreadsheetReader {
	
	public ODSSpreadsheetReader(File spreadsheetFile) {
		super(spreadsheetFile);
		read();
	}
	
	protected void read() {
		
		SpreadsheetDocument document =  null;
		
		try {
				List<String> tableNames = new ArrayList<String>();
				List<HashMap<String,String>> rowData = null;
				
				document = SpreadsheetDocument.loadDocument(spreadsheetFile);
				
				int totalNumberOfSheets = document.getSheetCount();
				for (int sheetIndex = 0; sheetIndex < (totalNumberOfSheets - 1); sheetIndex++) {
					Table sheet = document.getSheetByIndex(sheetIndex);
					
					// Find columns...
					Row headerRow = sheet.getRowByIndex(0);
					
					//If we don't have any columns then there's nothing we can do
					if (headerRow != null && headerRow.getCellCount() > 0) {
						String tableName = sheet.getTableName();
						tableNames.add(tableName);
					
						Row typedRow = sheet.getRowByIndex(1);
						
						List<String> columnNames = new ArrayList<String>();
						
						HashMap<String,ColumnType> columnNamesAndTypes = new HashMap<String,ColumnType>();

						for (int cellIndex = 0; cellIndex < headerRow.getCellCount(); cellIndex++) {
							Cell headerRowCell = headerRow.getCellByIndex(cellIndex);
							String cellName = headerRowCell.getStringValue();
									
							columnNames.add(cellName);
						
							Cell typedRowCell = null;
							if (typedRow != null) {
								typedRowCell = typedRow.getCellByIndex(cellIndex);
							}
						
							ColumnType columnType = ColumnType.STRING;
						
							String cellType = null;
							
							if (typedRowCell != null) {
								cellType = typedRowCell.getValueType();
							}
							
							if (cellType != null) {
								if (typedRowCell.getDisplayText().endsWith(".id")) {
									columnType = ColumnType.INTEGER_NUMBER;
								} else {
									if (cellType.equalsIgnoreCase("boolean") || cellType.equalsIgnoreCase("string")) {
										columnType = ColumnType.STRING;
									} else if (cellType.equalsIgnoreCase("currency") || cellType.equalsIgnoreCase("float")) {
										if (typedRowCell.getStringValue().indexOf('.') != -1) {
											columnType = ColumnType.DECIMAL_NUMBER;
										} else {
											columnType = ColumnType.INTEGER_NUMBER;
										}
									} else if (cellType.equalsIgnoreCase("date")) {
										columnType = ColumnType.DATE;
									}
								}
							}

							columnNamesAndTypes.put(headerRowCell.getStringValue(),
												    columnType);
				}
						
				tableDefinitions.put(tableName, columnNamesAndTypes);
				rowData = new ArrayList<HashMap<String,String>>();
				
				HashMap<String,String> columnData = null;
				
				int sheetRowCount = sheet.getRowCount();
				
				if (sheetRowCount > 1) {
					for (int rowIndex = 1; rowIndex <= (sheetRowCount - 1); rowIndex++) {
						Row dataRow = sheet.getRowByIndex(rowIndex);
						
						int dataRowCellCount = dataRow.getCellCount();
						
						columnData = new HashMap<String,String>();
						
						if (dataRowCellCount == headerRow.getCellCount()) {
							for (int cellIndex = 0; cellIndex <= (dataRowCellCount - 1); cellIndex++) {
								String colName = columnNames.get(cellIndex);
								Cell dataCell = dataRow.getCellByIndex(cellIndex);
	
								if (dataCell.getValueType() != null) {
									if (dataCell.getValueType().equalsIgnoreCase("string")) {
										columnData.put(colName,dataCell.getStringValue().trim());
									} else if (dataCell.getValueType().equalsIgnoreCase("date")) {
										// Default handling of date is to convert to string (if sqlite)
										// may need to implement 'per database column type mapping'
										// convert to ISO8601 string = YYYY-MM-DD HH:MM:SS.SSS
										SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
										Calendar date = dataCell.getDateValue();
										
										String dateFormattedCellValue = "";
										if (date != null) {
											dateFormattedCellValue = dateFormat.format(date.getTime());
										} else {
											dateFormattedCellValue = dataCell.getStringValue();
										}
										
										columnData.put(colName,dateFormattedCellValue);
									} else {
										if (columnNamesAndTypes.get(colName) == ColumnType.INTEGER_NUMBER) {
											columnData.put(colName, String.valueOf(new Double(dataCell.getDoubleValue()).intValue()).trim());
										} else {
											String formattedDecimalValue = dataCell.getDoubleValue().toString();
											columnData.put(colName, formattedDecimalValue);
										}
									}
								} else {
									columnData = null;
									break;
								}
							}
						}
						if (columnData != null) {
							rowData.add(columnData);
							columnData = null;
						}
					}
				}
				tableData.put(tableName, rowData);
			}
		}
		} catch (InvalidFormatException ife) {
			ife.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (document != null) {
				document.close();
			}
		}
	}
	
	

}
