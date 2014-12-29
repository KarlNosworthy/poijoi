package com.karlnosworthy.poijoi.io.xlsx;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.Writer;

@SupportsFormat(type = FormatType.XLSX)
public class XLSXSpreadsheetWriter implements Writer {

	@Override
	public void write(String output, PoijoiMetaData metaData, WriteType writeType) throws Exception {

		Workbook wb = new XSSFWorkbook();
		
		Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			
			Sheet sheet = wb.createSheet(tableName);
			TableDefinition table = tableDefinitions.get(tableName);
			Map<String, ColumnDefinition> columnDefinitions = table.getColumnDefinitions();
			
			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				Row headerRow = sheet.createRow(0);
				for (ColumnDefinition cd : columnDefinitions.values()) {
					Cell cell = headerRow.createCell(cd.getColumnIndex());
					cell.setCellValue(cd.getColumnName());
				}
			}
			
			// write out the data into the sheet
			if (writeType != WriteType.SCHEMA_ONLY) {
				List<HashMap<String,String>> tableData = metaData.getTableData(tableName);
				for (int rowIndex = 0; rowIndex < tableData.size(); rowIndex++) {
					int insertIndex = rowIndex;
					if (writeType != WriteType.DATA_ONLY) {
						insertIndex++; // increase the insert index if header included
					}
					Row row = sheet.createRow(insertIndex);
					HashMap<String,String> columnData = tableData.get(rowIndex);
					for (String columnName : columnData.keySet()) {
						ColumnDefinition columnDefinition = table.getColumnDefinition(columnName);
						Cell cell = row.createCell(columnDefinition.getColumnIndex());
						cell.setCellValue(columnData.get(columnName));
					}
				}
			}
			
		}
		
		// write out to the output... XLS file
		FileOutputStream fos = new FileOutputStream(new File(output));
		wb.write(fos);
		fos.close();
	}
}
