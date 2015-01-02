package com.karlnosworthy.poijoi.io.writer.ods;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;

public abstract class AbstractODSWriter<T> {

	abstract void write(T output, SpreadsheetDocument spreadsheetDocument)
			throws Exception;

	public final void write(T output, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {

		SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument
				.newSpreadsheetDocument();
		Table table = spreadsheetDocument.getSheetByIndex(0);

		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();

		int tableIndex = 0;

		for (String tableName : tableDefinitions.keySet()) {

			table.setTableName(tableName);

			TableDefinition tableDefinition = tableDefinitions.get(tableName);
			Map<String, ColumnDefinition> columnDefinitions = tableDefinition
					.getColumnDefinitions();

			// do headers
			if (writeType != WriteType.DATA_ONLY) {
				for (ColumnDefinition columnDefinition : columnDefinitions
						.values()) {
					Cell headerCell = table.getCellByPosition(
							columnDefinition.getColumnIndex(), 0);
					headerCell.setStringValue(columnDefinition.getColumnName()
							.toLowerCase());
				}
			}

			// do data
			if (writeType != WriteType.SCHEMA_ONLY) {
				Map<String, List<HashMap<String, Object>>> tableData = metaData
						.getTableData();

				List<HashMap<String, Object>> rowData = tableData
						.get(tableName);

				for (int rowIndex = 0; rowIndex < tableData.size(); rowIndex++) {
					for (ColumnDefinition columnDefinition : columnDefinitions
							.values()) {
						Cell dataCell = table.getCellByPosition(
								columnDefinition.getColumnIndex(),
								(1 + rowIndex));
						HashMap<String, Object> columnData = rowData
								.get(rowIndex);
						Object val = columnData.get(columnDefinition
								.getColumnName());
						if (val != null) {
							switch (columnDefinition.getColumnType()) {
							case DATE:
								Calendar cal = Calendar.getInstance();
								cal.setTime((Date) val);
								dataCell.setDateValue(cal);
								break;
							case DECIMAL_NUMBER:
								dataCell.setDoubleValue((Double) val);
								break;
							case INTEGER_NUMBER:
								Double d = new Double(val.toString());
								dataCell.setDoubleValue(d);
								break;
							default:
								dataCell.setStringValue(val.toString());
							}
						} else {
							dataCell.setStringValue(null); // set null value
						}
					}
				}
			}

			tableIndex += 1;

			if (tableIndex < tableDefinitions.size()) {
				table = spreadsheetDocument.addTable();
			}
		}
		write(output, spreadsheetDocument);
	}

}
