package com.karlnosworthy.poijoi.io.reader.xls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.io.reader.spreadsheet.SpreadsheetIndexDefinitionReader;
import com.karlnosworthy.poijoi.io.reader.spreadsheet.SpreadsheetTableDefinitionReader;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic XLS access and then specific
 * implementations will have to handle how the {@link Workbook} is initially
 * loaded.
 * 
 * @author john.bartlett
 * @author Karl Nosworthy
 *
 * @param <T>
 *            The source Type
 */
public abstract class AbstractXLSReader<T> {

	protected SpreadsheetIndexDefinitionReader indexDefinitionReader;
	protected SpreadsheetTableDefinitionReader tableDefinitionReader;


	protected AbstractXLSReader() {
		super();
		this.indexDefinitionReader = new XLSIndexDefinitionReader();
		this.tableDefinitionReader = new XLSTableDefinitionReader();
	}


	abstract boolean isValidInput(T input);
	
	/**
	 * Get a {@link Workbook} based on the source type
	 */
	abstract Workbook getWorkbook(T source) throws Exception;

	/**
	 * Reads in a XLS representation of a database and converts it into a
	 * {@link PoiJoiMetaData} object which holds the table structures and
	 * optionally the database data.
	 * 
	 * @param input
	 *            The input source of the data (e.g. java.io.File etc)
	 * @param readData
	 *            Whether or not to read the data or just the database structure
	 * @return a {@link PoiJoiMetaData} holding the table structures and
	 *         optionally the table data
	 */
	public final PoiJoiMetaData read(T input, boolean readData)
			throws Exception {
		
		if (!isValidInput(input)) {
			return null;
		}
		
		Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		Map<String, List<IndexDefinition>> indexDefinitions = new HashMap<String, List<IndexDefinition>>();

		Workbook workbook = getWorkbook(input);

		String[] nonInternalSheetNames = determineNonInternalSheetNames(workbook);

		// INDEXES/INDICES
		Sheet indexDefinitionSheet = (Sheet) indexDefinitionReader.findIndexDefinitionSheet(workbook);
		if (indexDefinitionSheet != null) {
			indexDefinitions = indexDefinitionReader.readDefinitions(indexDefinitionSheet);
		}

		// DATA TABLES
		if (nonInternalSheetNames != null && nonInternalSheetNames.length > 0) {
			for (String sheetName : nonInternalSheetNames) {
				Sheet sheet = workbook.getSheet(sheetName);

				TableDefinition tableDefinition = tableDefinitionReader.read(sheet, indexDefinitions);
				if (tableDefinition != null) {
					tables.put(tableDefinition.getTableName(), tableDefinition);
					if (readData) {
						tableData.put(tableDefinition.getTableName(),
								readData(sheet, tableDefinition));
					}
				}
			}
		}

		return new PoiJoiMetaData(readData, tables, tableData);
	}

	private List<HashMap<String, Object>> readData(Sheet sheet,
			TableDefinition tableDefinition) {
		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();

		if (sheet.getLastRowNum() > 1) {
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row dataRow = sheet.getRow(rowIndex);

				HashMap<String, Object> columnData = new HashMap<String, Object>();
				for (int cellIndex = dataRow.getFirstCellNum(); cellIndex <= (dataRow
						.getLastCellNum() - 1); cellIndex++) {

					ColumnDefinition columnDefinition = tableDefinition
							.getColumnDefinition(cellIndex);

					String colName = columnDefinition.getColumnName();
					Cell dataCell = dataRow.getCell(cellIndex);

					if (dataCell != null) {
						if (dataCell.getCellType() == Cell.CELL_TYPE_STRING) {
							columnData.put(colName, dataCell.getStringCellValue()
									.trim());
						} else if (HSSFDateUtil.isCellDateFormatted(dataCell)) {
							columnData.put(colName, dataCell.getDateCellValue());
						} else {
							Double d = new Double(dataCell.getNumericCellValue());
							if (columnDefinition.getColumnType() == ColumnType.INTEGER_NUMBER) {
								columnData.put(colName, d.intValue());
							} else {
								columnData.put(colName, d);
							}
						}
					}
				}
				rowData.add(columnData);
			}
		}
		return rowData;
	}

	private String[] determineNonInternalSheetNames(Workbook workbook) {
		List<String> nonInternalSheetNamesList = new ArrayList<String>();

		for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
			String sheetName = workbook.getSheetName(sheetNumber);

			if (!indexDefinitionReader.isIndexSheet(sheetName)) {
				nonInternalSheetNamesList.add(sheetName);
			}
		}

		if (!nonInternalSheetNamesList.isEmpty()) {
			return nonInternalSheetNamesList.toArray(new String[nonInternalSheetNamesList.size()]);
		}
		return null;
	}
}

