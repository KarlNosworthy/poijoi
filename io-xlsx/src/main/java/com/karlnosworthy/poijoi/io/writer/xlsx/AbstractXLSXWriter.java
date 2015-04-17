package com.karlnosworthy.poijoi.io.writer.xlsx;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic XLSX access and then specific
 * implementations will have to handle how they write the {@link Workbook} out.
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The output type
 */
public abstract class AbstractXLSXWriter<T> {

	abstract boolean isValidOutput(T output);
	
	/**
	 * Write the generated {@link Workbook} to a given output
	 * 
	 * @param output
	 *            The output mechanism
	 * @param workbook
	 *            The workbook to output
	 */
	abstract boolean write(T output, Workbook workbook) throws Exception;
	
	/**
	 * Writes out an XLSX File to reflect the table structures and optionally the
	 * data stored in the {@link PoiJoiMetaData}.
	 * 
	 * @param output
	 *            The mechanism for outputting the data (e.g. java.io.File etc)
	 * @param metaData
	 *            a {@link PoiJoiMetaData} holding the table structures and
	 *            optionally the table data
	 * @param writeType
	 *            Rules around what to write
	 */
	public final boolean write(T output, PoiJoiMetaData metaData, WriteType writeType) throws Exception {

		if (!isValidOutput(output) || !isValidMetadata(metaData)) {
			return false;
		}
		
		Workbook wb = new XSSFWorkbook();
		
		Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			
			Sheet sheet = wb.createSheet(tableName);
			TableDefinition table = tableDefinitions.get(tableName);
			List<ColumnDefinition> columnDefinitions = table.getColumnDefinitions();
			
			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				Row headerRow = sheet.createRow(0);
				for (ColumnDefinition cd : columnDefinitions) {
					Cell cell = headerRow.createCell(cd.getColumnIndex());
					cell.setCellValue(cd.getColumnName());
				}
			}
			
			// write out the data into the sheet
			if (writeType != WriteType.SCHEMA_ONLY) {
				List<HashMap<String, Object>> tableData = metaData.getTableData(tableName);
				for (int rowIndex = 0; rowIndex < tableData.size(); rowIndex++) {
					int insertIndex = rowIndex;
					if (writeType != WriteType.DATA_ONLY) {
						insertIndex++; // increase the insert index if header included
					}
					Row row = sheet.createRow(insertIndex);
					HashMap<String,Object> columnData = tableData.get(rowIndex);
					for (String columnName : columnData.keySet()) {
						ColumnDefinition columnDefinition = table.getColumnDefinition(columnName);
						Cell cell = row.createCell(columnDefinition.getColumnIndex());
						switch (columnDefinition.getColumnType()) {
							case DATE:
								cell.setCellValue((Date) columnData.get(columnName));
								break;
							default:
								cell.setCellValue(columnData.get(columnName).toString());
						}
					}
				}
			}
			
		}
		return write(output, wb);
	}
	
	private boolean isValidMetadata(PoiJoiMetaData metadata) {
		if (metadata == null) {
			return false;
		} else {
			if (metadata.getTableDefinitions() == null || metadata.getTableDefinitions().isEmpty()) {
				return false;
			} else if (metadata.isReadData() && metadata.getTableData() == null) {
				return false;
			}
		}
		return true;
	}

}
