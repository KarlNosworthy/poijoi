package com.karlnosworthy.poijoi.io.ods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.Reader;
import com.karlnosworthy.poijoi.io.SupportsFormat;

@SupportsFormat(type = FormatType.ODS)
public final class ODSSpreadsheetReader implements Reader {

	public PoijoiMetaData read(String spreadsheetFile, boolean readData)
			throws Exception {
		SpreadsheetDocument document = null;
		try {
			Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
			Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
			document = SpreadsheetDocument.loadDocument(spreadsheetFile);
			int totalNumberOfSheets = document.getSheetCount();
			for (int sheetIndex = 0; sheetIndex < (totalNumberOfSheets - 1); sheetIndex++) {
				Table sheet = document.getSheetByIndex(sheetIndex);
				TableDefinition tableDefinition = parseSheetMeta(sheet);
				if (tableDefinition == null) {
					continue; // couldn't read table definition
				}
				tables.put(tableDefinition.getTableName(), tableDefinition);
				if (readData) {
					tableData.put(tableDefinition.getTableName(),
							readData(sheet, tableDefinition));
				}
			}
			return new PoijoiMetaData(readData, tables, tableData);
		} finally {
			if (document != null) {
				document.close();
			}
		}
	}

	private TableDefinition parseSheetMeta(Table sheet) {

		// Find columns...
		Row headerRow = sheet.getRowByIndex(0);

		// If we don't have any columns then there's nothing we can do
		if (headerRow != null && headerRow.getCellCount() > 0) {
			String tableName = sheet.getTableName();

			Row typedRow = sheet.getRowByIndex(1);

			HashMap<String, ColumnDefinition> columns = new HashMap<String, ColumnDefinition>();

			for (int cellIndex = 0; cellIndex < headerRow.getCellCount(); cellIndex++) {
				Cell headerRowCell = headerRow.getCellByIndex(cellIndex);
				String cellName = headerRowCell.getStringValue();
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
						if (cellType.equalsIgnoreCase("boolean")
								|| cellType.equalsIgnoreCase("string")) {
							columnType = ColumnType.STRING;
						} else if (cellType.equalsIgnoreCase("currency")
								|| cellType.equalsIgnoreCase("float")) {
							if (typedRowCell.getStringValue().indexOf('.') != -1) {
								columnType = ColumnType.DECIMAL_NUMBER;
							} else {
								columnType = ColumnType.INTEGER_NUMBER;
							}
						} else if (cellType.equalsIgnoreCase("date")) {
							columnType = ColumnType.DATE;
						}
					}
					ColumnDefinition cd = new ColumnDefinition(cellName,
							cellIndex, columnType);
					columns.put(cellName, cd);
				}
			}
			if (!columns.isEmpty()) {
				return new TableDefinition(tableName, columns);
			}
		}
		return null;
	}

	private List<HashMap<String, Object>> readData(Table sheet,
			TableDefinition tableDefinition) {
		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
		int sheetRowCount = sheet.getRowCount();
		if (sheetRowCount > 1) {
			for (int rowIndex = 1; rowIndex <= (sheetRowCount - 1); rowIndex++) {
				Row dataRow = sheet.getRowByIndex(rowIndex);
				int dataRowCellCount = dataRow.getCellCount();
				HashMap<String, Object> columnData = new HashMap<String, Object>();
				if (dataRowCellCount == tableDefinition.getColumnCount()) {
					for (int cellIndex = 0; cellIndex <= (dataRowCellCount - 1); cellIndex++) {
						ColumnDefinition columnDefinition = tableDefinition
								.getColumnDefinition(cellIndex);
						String colName = columnDefinition.getColumnName();
						Cell dataCell = dataRow.getCellByIndex(cellIndex);
						if (dataCell.getValueType() != null) {
							if (dataCell.getValueType().equalsIgnoreCase(
									"string")) {
								columnData.put(colName, dataCell
										.getStringValue().trim());
							} else if (dataCell.getValueType()
									.equalsIgnoreCase("date")) {
								columnData.put(colName, dataCell.getDateValue()
										.getTime());
							} else {
								Double d = new Double(dataCell.getDoubleValue());
								if (columnDefinition.getColumnType() == ColumnType.INTEGER_NUMBER) {
									columnData.put(colName, d.intValue());
								} else {
									columnData.put(colName, d);
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
		return rowData;
	}
}
