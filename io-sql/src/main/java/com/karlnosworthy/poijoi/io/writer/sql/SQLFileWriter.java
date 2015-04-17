package com.karlnosworthy.poijoi.io.writer.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.Writer;
import com.karlnosworthy.poijoi.jdbc.SQLStatementCreator;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * 
 * @author Karl Nosworthy
 * @since 1.0
 */


@SupportsFormat(type = "SQL")
public class SQLFileWriter implements Writer<File> {

	@Override
	public boolean write(File output, PoiJoiMetaData metaData, WriteType writeType) throws Exception {
		
		if (output == null || output.isDirectory() || output.exists()) { 
			return false;
		} else if (metaData == null || metaData.isEmpty()) {
			return false;
		}
		
		SQLStatementCreator sqlStatementWriter = new SQLStatementCreator();
		
		StringBuffer buffer = new StringBuffer();
		
		if (writeType != WriteType.DATA_ONLY) {
			for (String tableName : metaData.getTableDefinitions().keySet()) {
				TableDefinition tableDefinition = metaData.getTableDefinition(tableName);
				buffer.append(sqlStatementWriter.buildCreateTableStatement(tableDefinition));
				buffer.append("\n");
			}
		}
		
		if (writeType != WriteType.SCHEMA_ONLY) {
			for (String tableName : metaData.getTableDefinitions().keySet()) {
				TableDefinition tableDefinition = metaData.getTableDefinition(tableName);
				List<HashMap<String, Object>> tableData = metaData.getTableData(tableName);

				for (int tableDataRowIndex = 0; tableDataRowIndex < tableData.size(); tableDataRowIndex ++) {
					Map<String,Object> rowDataToInsert = tableData.get(tableDataRowIndex);
					buffer.append(sqlStatementWriter.buildInsertTableStatement(tableDefinition, rowDataToInsert));
					buffer.append("\n");
				}
			}
		}
		
		FileOutputStream fileOutputStream = new FileOutputStream(output);
		fileOutputStream.write(buffer.toString().getBytes());
		fileOutputStream.close();
		return true;
	}
}
