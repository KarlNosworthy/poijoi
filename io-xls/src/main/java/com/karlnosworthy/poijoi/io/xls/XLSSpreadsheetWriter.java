package com.karlnosworthy.poijoi.io.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.Writer;

@SupportsFormat(type = FormatType.XLS)
public class XLSSpreadsheetWriter implements Writer {

	@Override
	public void write(String output, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {

		HSSFWorkbook wb = new HSSFWorkbook();
		
		Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
		for (String tableName : tableDefinitions.keySet()) {
			HSSFSheet sheet = wb.createSheet(tableName);
			TableDefinition table = tableDefinitions.get(tableName);
			Map<String, ColumnDefinition> columnDefinitions = table.getColumnDefinitions();
			
			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				HSSFRow headerRow = sheet.createRow(0);
				for (ColumnDefinition cd : columnDefinitions.values()) {
					HSSFCell cell = headerRow.createCell(cd.getColumnIndex());
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
					HSSFRow row = sheet.createRow(insertIndex);
					HashMap<String,String> columnData = tableData.get(rowIndex);
					for (String columnName : columnData.keySet()) {
						ColumnDefinition columnDefinition = table.getColumnDefinition(columnName);
						HSSFCell cell = row.createCell(columnDefinition.getColumnIndex());
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
