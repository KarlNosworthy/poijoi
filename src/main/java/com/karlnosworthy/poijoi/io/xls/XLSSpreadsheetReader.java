package com.karlnosworthy.poijoi.io.xls;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.io.Reader;

public final class XLSSpreadsheetReader implements Reader {

	public PoijoiMetaData read(String spreadsheetFile, ReadType readType)
			throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(
				spreadsheetFile));
		Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
		Map<String, List<HashMap<String, String>>> tableData = new HashMap<String, List<HashMap<String, String>>>();
		int totalNumberOfSheets = workbook.getNumberOfSheets();
		for (int sheetIndex = 0; sheetIndex < (totalNumberOfSheets - 1); sheetIndex++) {
			HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
			TableDefinition tableDefinition = parseSheetMeta(sheet, readType);
			if (tableDefinition == null) {
				continue; // couldn't read table definition
			}
			tables.put(tableDefinition.getTableName(), tableDefinition);
			if (readType != ReadType.SCHEMA) {
				tableData.put(tableDefinition.getTableName(),
						readData(sheet, tableDefinition));
			}
		}
		return new PoijoiMetaData(readType, tables, tableData);
	}

	private TableDefinition parseSheetMeta(HSSFSheet sheet, ReadType readType) {

		DataFormatter dataFormatter = new DataFormatter();

		// Find columns...
		HSSFRow headerRow = sheet.getRow(0);

		// If we don't have any columns then there's nothing we can do
		if (headerRow != null) {
			String tableName = sheet.getSheetName();
			HSSFRow typedRow = sheet.getRow(1);
			HashMap<String, ColumnDefinition> columns = new HashMap<String, ColumnDefinition>();
			for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
				HSSFCell headerRowCell = headerRow.getCell(cellIndex);
				String cellName = headerRowCell.getStringCellValue();

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
						String formattedValue = dataFormatter
								.formatCellValue(typedRowCell);

						if (formattedValue.contains(".")) {
							columnType = ColumnType.DECIMAL_NUMBER;
						} else {
							columnType = ColumnType.INTEGER_NUMBER;
						}
					}
					break;
				}
				ColumnDefinition cd = new ColumnDefinition(cellName, cellIndex,
						columnType);
				columns.put(cellName, cd);
			}
			return new TableDefinition(tableName, columns);
		}
		return null;
	}

	private List<HashMap<String, String>> readData(HSSFSheet sheet,
			TableDefinition tableDefinition) {
		DataFormatter dataFormatter = new DataFormatter();
		List<HashMap<String, String>> rowData = new ArrayList<HashMap<String, String>>();
		if (sheet.getLastRowNum() > 1) {
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				HSSFRow dataRow = sheet.getRow(rowIndex);

				HashMap<String, String> columnData = new HashMap<String, String>();
				for (int cellIndex = dataRow.getFirstCellNum(); cellIndex <= (dataRow
						.getLastCellNum() - 1); cellIndex++) {
					ColumnDefinition columnDefinition = tableDefinition
							.getColumnDefinition(cellIndex);
					String colName = columnDefinition.getColumnName();
					HSSFCell dataCell = dataRow.getCell(cellIndex);

					if (dataCell.getCellType() == Cell.CELL_TYPE_STRING) {
						columnData.put(colName, dataCell.getStringCellValue()
								.trim());
					} else if (HSSFDateUtil.isCellDateFormatted(dataCell)) {
						// Default handling of date is to convert to
						// string (if sqlite)
						// may need to implement 'per database
						// column type mapping'
						// convert to ISO8601 string = YYYY-MM-DD
						// HH:MM:SS.SSS
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS");
						Date date = dataCell.getDateCellValue();

						String dateFormattedCellValue = "";
						if (date != null) {
							dateFormattedCellValue = dateFormat.format(date);
						} else {
							dateFormattedCellValue = dataCell
									.getStringCellValue();
						}

						columnData.put(colName, dateFormattedCellValue);
					} else {
						if (columnDefinition.getColumnType() == ColumnType.INTEGER_NUMBER) {
							columnData.put(
									colName,
									String.valueOf(
											new Double(dataCell
													.getNumericCellValue())
													.intValue()).trim());
						} else {
							String formattedDecimalValue = dataFormatter
									.formatCellValue(dataCell);
							columnData.put(colName, formattedDecimalValue);
						}
					}
				}
				rowData.add(columnData);
			}
		}
		return rowData;
	}
}
