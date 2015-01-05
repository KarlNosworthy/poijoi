package com.karlnosworthy.poijoi.io.reader.ods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic ODS access and then specific
 * implementations will have to handle how the {@link SpreadsheetDocument} is
 * initially loaded.
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The source Type
 */
public abstract class AbstractODSReader<T> {

	protected static final Logger logger = LoggerFactory
			.getLogger(AbstractODSReader.class);
	
	abstract boolean isValidInput(T input);

	/**
	 * Get a {@link SpreadsheetDocument} based on the input type
	 */
	abstract SpreadsheetDocument getDocument(T input) throws Exception;

	/**
	 * Reads in a representation of a database and converts it into a
	 * {@link PoijoiMetaData} object which holds the table structures and
	 * optionally the database data.
	 * 
	 * @param input
	 *            The input of the data (e.g. java.io.File etc)
	 * @param readData
	 *            Whether or not to read the data or just the database structure
	 * @return a {@link PoijoiMetaData} holding the table structures and
	 *         optionally the table data
	 */
	public final PoijoiMetaData read(T input, boolean readData)
			throws Exception {
		
		if (!isValidInput(input)) {
			return null;
		}
		
		SpreadsheetDocument document = null;
		try {
			Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
			Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
			document = getDocument(input);
			int totalNumberOfSheets = document.getSheetCount();
			for (int sheetIndex = 0; sheetIndex < totalNumberOfSheets; sheetIndex++) {
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

	/**
	 * Parse the table structure stored within a sheet
	 */
	private TableDefinition parseSheetMeta(Table sheet) {

		String tableName = sheet.getTableName();

		// Find header column
		Row headerRow = sheet.getRowByIndex(0);

		// If we don't have a valid header skip
		if (!validHeader(headerRow)) {
			logger.warn("Couldn't find valid header row on {}, so skipping",
					tableName);
			return null;
		}

		// first row of data
		Row dataRow = sheet.getRowByIndex(1);
		if (dataRow == null) {
			logger.warn("Couldn't find data row in {}, so skipping", tableName);
			return null;
		}

		// iterate over all columns of first row and work out types based on
		// the data
		HashMap<String, ColumnDefinition> columns = new HashMap<String, ColumnDefinition>();
		for (int cellIndex = 0; cellIndex < headerRow.getCellCount(); cellIndex++) {
			Cell headerRowCell = headerRow.getCellByIndex(cellIndex);
			String cellName = headerRowCell.getStringValue();
			Cell typedRowCell = dataRow.getCellByIndex(cellIndex);
			ColumnType columnType = ColumnType.STRING;
			String cellType = typedRowCell.getValueType();
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
			ColumnDefinition cd = new ColumnDefinition(cellName, cellIndex,
					columnType);
			columns.put(cellName, cd);
		}
		if (!columns.isEmpty()) {
			return new TableDefinition(tableName, columns);
		}
		return null;
	}

	/**
	 * Check for a valid header row
	 */
	private boolean validHeader(Row headerRow) {
		if (headerRow == null || headerRow.getCellCount() < 1) {
			return false;
		}
		// if the header has cells make sure the first has content
		Cell headerCell = headerRow.getCellByIndex(0);
		return headerCell.getStringValue() != null
				&& !headerCell.getStringValue().trim().isEmpty();
	}

	/**
	 * Read the data stored in a sheet based on the passed in
	 * {@link TableDefinition}
	 */
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
