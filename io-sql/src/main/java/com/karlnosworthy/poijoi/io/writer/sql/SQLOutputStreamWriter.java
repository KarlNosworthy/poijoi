package com.karlnosworthy.poijoi.io.writer.sql;

import java.io.IOException;
import java.io.OutputStream;
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
public class SQLOutputStreamWriter implements Writer<OutputStream> {

	@Override
	public boolean write(OutputStream output, PoiJoiMetaData metaData, WriteType writeType) throws Exception {
		
		if (isValidOutput(output) && isValidMetadata(metaData)) {
			
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
			
			output.write(buffer.toString().getBytes());
			return true;
		}
		return false;
	}
	
	boolean isValidOutput(OutputStream output) {
		if (output == null) {
			return false;
		} else {
			try {
				output.flush();
			} catch (IOException ioException) {
				return false;
			}
		}
		return true;
	}
	
	boolean isValidMetadata(PoiJoiMetaData metadata) {
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
