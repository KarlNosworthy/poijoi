package com.karlnosworthy.poijoi.io.writer.xls;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic XLS access and then specific
 * implementations will have to handle how they write the {@link Workbook} out.
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The output type
 */
public abstract class AbstractXLSWriter<T> {
	
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
	 * Writes out an XLS File to reflect the table structures and optionally the
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
	public final boolean write(T output, PoiJoiMetaData metaData,
			WriteType writeType) throws Exception {

		if (!isValidOutput(output) || !isValidMetadata(metaData)) {
			return false;
		}
		
		HSSFWorkbook wb = new HSSFWorkbook();

		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			HSSFSheet sheet = wb.createSheet(tableName);
			TableDefinition table = tableDefinitions.get(tableName);
			List<ColumnDefinition> columnDefinitions = table.getColumnDefinitions();

			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				HSSFRow headerRow = sheet.createRow(0);
				for (ColumnDefinition cd : columnDefinitions) {
					HSSFCell cell = headerRow.createCell(cd.getColumnIndex());
					cell.setCellValue(cd.getColumnName());
				}
			}

			// write out the data into the sheet
			if (writeType != WriteType.SCHEMA_ONLY) {
				List<HashMap<String, Object>> tableData = metaData
						.getTableData(tableName);
				for (int rowIndex = 0; rowIndex < tableData.size(); rowIndex++) {
					int insertIndex = rowIndex;
					if (writeType != WriteType.DATA_ONLY) {
						insertIndex++; // increase the insert index if header
										// included
					}
					HSSFRow row = sheet.createRow(insertIndex);
					HashMap<String, Object> columnData = tableData
							.get(rowIndex);
					for (String columnName : columnData.keySet()) {
						ColumnDefinition columnDefinition = table
								.getColumnDefinition(columnName);
						HSSFCell cell = row.createCell(columnDefinition
								.getColumnIndex());
						switch (columnDefinition.getColumnType()) {
						case DATE:
							cell.setCellValue((Date) columnData.get(columnName));
							break;
						default:
							cell.setCellValue(columnData.get(columnName)
									.toString());
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
