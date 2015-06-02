package com.karlnosworthy.poijoi.io.reader.xls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.model.IndexDefinition;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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

	protected IndexDefinitionReader indexDefinitionReader;
	protected TableDefinitionReader tableDefinitionReader;


	protected AbstractXLSReader() {
		super();
		this.indexDefinitionReader = new IndexDefinitionReader();
		this.tableDefinitionReader = new TableDefinitionReader();
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
		Sheet indexDefinitionSheet = indexDefinitionReader.findIndexDefinitionSheet(workbook);
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

class IndexDefinitionReader {

	private final int CELL_INDEX_NAME = 0;
	private final int CELL_TABLE_NAME = 1;
	private final int CELL_TABLE_COLUMNS = 2;
	private final int CELL_UNIQUE_FLAG = 3;


	IndexDefinitionReader() {
		super();
	}

	public boolean isIndexSheet(String sheetName) {
		if (sheetName.equalsIgnoreCase("indexes") ||
			sheetName.equalsIgnoreCase("indices")) {
			return true;
		}
		return false;
	}

	public Sheet findIndexDefinitionSheet(Workbook workbook) {

		if (workbook != null && workbook.getNumberOfSheets() > 0) {
			for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
				String sheetName = workbook.getSheetName(sheetNumber);
				if (isIndexSheet(sheetName)) {
					return workbook.getSheetAt(sheetNumber);
				}
			}
		}
		return null;
	}

	public Map<String, List<IndexDefinition>> readDefinitions(Sheet sheet) {
		Map<String, List<IndexDefinition>> tableIndexDefinitions = new HashMap<String, List<IndexDefinition>>();

		if (sheet.getFirstRowNum() == sheet.getLastRowNum()) {
			IndexDefinition indexDefinition = readDefinition(sheet.getRow(0));

			if (indexDefinition != null) {
				List<IndexDefinition> indexDefinitions = new ArrayList<IndexDefinition>();
				indexDefinitions.add(indexDefinition);

				tableIndexDefinitions.put(indexDefinition.getTableName(), indexDefinitions);
			}
		} else {
			int numberOfIndexes = sheet.getLastRowNum() - sheet.getFirstRowNum();

			for (int rowNumber = sheet.getFirstRowNum(); rowNumber < numberOfIndexes; rowNumber++) {
				IndexDefinition indexDefinition = readDefinition(sheet.getRow(rowNumber));

				if (indexDefinition != null) {
					List<IndexDefinition> indexDefinitions = null;

					if (tableIndexDefinitions.containsKey(indexDefinition.getTableName())) {
						indexDefinitions = tableIndexDefinitions.get(indexDefinition.getTableName());
					} else {
						indexDefinitions = new ArrayList<IndexDefinition>();
						tableIndexDefinitions.put(indexDefinition.getTableName(), indexDefinitions);
					}

					indexDefinitions.add(indexDefinition);
				}
			}
		}

		return tableIndexDefinitions;
	}

	public IndexDefinition readDefinition(Row row) {

		IndexDefinition indexDefinition = null;

		String indexName = getValueForCell(row, CELL_INDEX_NAME);
		String tableName = getValueForCell(row, CELL_TABLE_NAME);
		String columns = getValueForCell(row, CELL_TABLE_COLUMNS);

		String[] columnNames=  columns.split(",");

		indexDefinition = new IndexDefinition(indexName, tableName, columnNames, false);

		return indexDefinition;
	}

	private String getValueForCell(Row row, int cellIndex) {
		Cell cell = row.getCell(cellIndex);
		return cell.getStringCellValue();
	}
}

class TableDefinitionReader {

	private DataFormatter dataFormatter;


	TableDefinitionReader() {
		super();
		this.dataFormatter = new DataFormatter();
	}

	public TableDefinition read(Sheet sheet) {
		return read(sheet, null);
	}

	public TableDefinition read(Sheet sheet, Map<String,List<IndexDefinition>> indexDefinitions) {

		TableDefinition tableDefinition = null;

		// Find header column
		Row headerRow = sheet.getRow(sheet.getFirstRowNum());

		String tableName = sheet.getSheetName();

		// If we don't have any columns then there's nothing we can do
		if (headerRow != null) {
			Row dataRow = sheet.getRow(1 + sheet.getFirstRowNum());

			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
				Cell headerRowCell = headerRow.getCell(cellIndex);
				String cellName = headerRowCell.getStringCellValue();

				Cell typedRowCell = null;
				if (dataRow != null) {
					typedRowCell = dataRow.getCell(cellIndex);
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

				columns.add(new ColumnDefinition(cellName, cellIndex, columnType));
			}

			if (indexDefinitions != null && indexDefinitions.containsKey(tableName)) {
				tableDefinition = new TableDefinition(tableName, columns, indexDefinitions.get(tableName));
			} else {
				tableDefinition = new TableDefinition(tableName, columns);
			}
		}
		return tableDefinition;
	}
}
