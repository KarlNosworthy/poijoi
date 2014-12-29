package com.karlnosworthy.poijoi.io.ods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.Writer;

@SupportsFormat(type = FormatType.ODS)
public class ODSSpreadsheetWriter implements Writer {

	@Override
	public void write(String output, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {

		
		SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.newSpreadsheetDocument();
		Table table = spreadsheetDocument.getSheetByIndex(0);

		Map<String, TableDefinition> tableDefinitions = metaData.getTableDefinitions();
		
		int tableIndex = 0;
		
		for (String tableName : tableDefinitions.keySet()) {
			
			table.setTableName(tableName);
			
			TableDefinition tableDefinition = tableDefinitions.get(tableName);
			Map<String, ColumnDefinition> columnDefinitions = tableDefinition.getColumnDefinitions();
			
			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				for (ColumnDefinition columnDefinition : columnDefinitions.values()) {
					Cell headerCell = table.getCellByPosition(columnDefinition.getColumnIndex(),0);
					headerCell.setStringValue(columnDefinition.getColumnName().toLowerCase());
				}
			}
			
			// do data
			if (writeType != WriteType.SCHEMA_ONLY) {
				Map<String, List<HashMap<String, String>>> tableData = metaData.getTableData();
				
				List<HashMap<String, String>> rowData = tableData.get(tableName);
				
				for (int rowIndex = 0; rowIndex < tableData.size(); rowIndex++) {
					for (ColumnDefinition columnDefinition : columnDefinitions.values()) {
						Cell dataCell = table.getCellByPosition(columnDefinition.getColumnIndex(),(1 + rowIndex));
						HashMap<String, String> columnData = rowData.get(rowIndex);
						dataCell.setStringValue(columnData.get(columnDefinition.getColumnName()));
					}
				}
			}
			
			tableIndex += 1;
			
			if (tableIndex < tableDefinitions.size()) {
				table = spreadsheetDocument.addTable();
			}
		}
		spreadsheetDocument.save(output);
	}

}
