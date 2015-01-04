package com.karlnosworthy.poijoi.io.reader.xls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic XLS access and then specific
 * implementations will have to handle how the {@link Workbook} is initially
 * loaded.
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The source Type
 */
public abstract class AbstractXLSReader<T> {

	/**
	 * Get a {@link Workbook} based on the source type
	 */
	abstract Workbook getWorkbook(T source) throws Exception;

	/**
	 * Reads in a XLS representation of a database and converts it into a
	 * {@link PoijoiMetaData} object which holds the table structures and
	 * optionally the database data.
	 * 
	 * @param source
	 *            The source of the data (e.g. java.io.File etc)
	 * @param readData
	 *            Whether or not to read the data or just the database structure
	 * @return a {@link PoijoiMetaData} holding the table structures and
	 *         optionally the table data
	 */
	public final PoijoiMetaData read(T source, boolean readData)
			throws Exception {
		Workbook workbook = getWorkbook(source);
		Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();
		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		int totalNumberOfSheets = workbook.getNumberOfSheets();
		for (int sheetIndex = 0; sheetIndex < totalNumberOfSheets; sheetIndex++) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
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
	}

	private TableDefinition parseSheetMeta(Sheet sheet) {

		DataFormatter dataFormatter = new DataFormatter();

		// Find header column
		Row headerRow = sheet.getRow(0);

		// If we don't have any columns then there's nothing we can do
		if (headerRow != null) {
			String tableName = sheet.getSheetName();
			Row dataRow = sheet.getRow(1);
			HashMap<String, ColumnDefinition> columns = new HashMap<String, ColumnDefinition>();
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
				ColumnDefinition cd = new ColumnDefinition(cellName, cellIndex,
						columnType);
				columns.put(cellName, cd);
			}
			return new TableDefinition(tableName, columns);
		}
		return null;
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
				rowData.add(columnData);
			}
		}
		return rowData;
	}

}
