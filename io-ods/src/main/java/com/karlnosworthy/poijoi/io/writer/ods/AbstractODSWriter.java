package com.karlnosworthy.poijoi.io.writer.ods;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.UnsupportedMapping;
import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * Abstract class used to handle all the generic ODS access and then specific
 * implementations will have to handle how they write the
 * {@link SpreadsheetDocument} out.
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The output type
 */
public abstract class AbstractODSWriter<T> {

	abstract boolean isValidOutput(T output);
	
	/**
	 * Write the generated {@link SpreadsheetDocument} to a given output
	 * 
	 * @param output
	 *            The output mechanism
	 * @param spreadsheetDocument
	 *            The spreadsheet to output
	 */
	abstract boolean write(T output, SpreadsheetDocument spreadsheetDocument)
			throws Exception;

	/**
	 * Writes out a ODS File to reflect the table structures and optionally the
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

		// create a new spreadsheet document ready to write out to the output
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

			// create the header row using the column definitions
			if (writeType != WriteType.DATA_ONLY) {
				for (ColumnDefinition columnDefinition : columnDefinitions
						.values()) {
					Cell headerCell = table.getCellByPosition(
							columnDefinition.getColumnIndex(), 0);
					headerCell.setStringValue(columnDefinition.getColumnName()
							.toLowerCase());
				}
			}

			// if the write type is correct output the data into the spreadsheet
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
								1 + rowIndex);
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
							case STRING:
								dataCell.setStringValue(val.toString());
								break;
							default:
								throw new UnsupportedMapping(
										"Cannot map column type: "
												+ columnDefinition
														.getColumnType());
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

		// write out the spreadsheet
		return write(output, spreadsheetDocument);
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
