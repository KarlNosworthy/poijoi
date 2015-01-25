package com.karlnosworthy.poijoi.io.writer.sql;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.Writer;
import com.karlnosworthy.poijoi.jdbc.SQLStatementCreator;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

/**
 * 
 * @author Karl Nosworthy
 * @since 1.0
 */
@SupportsFormat(type = "SQL")
public class SQLOutputStreamWriter implements Writer<OutputStream> {

	@Override
	public boolean write(OutputStream output, PoijoiMetaData metaData, WriteType writeType) throws Exception {
		
		if (isValidOutput(output) && isValidMetadata(metaData)) {
			
			SQLStatementCreator sqlStatementWriter = new SQLStatementCreator();
			
			StringBuffer buffer = new StringBuffer();
			
			if (writeType != WriteType.DATA_ONLY) {
				for (String tableName : metaData.getTableDefinitions().keySet()) {
					TableDefinition tableDefinition = metaData.getTableDefinition(tableName);
					buffer.append(sqlStatementWriter.buildCreateTableSQL(tableDefinition));
					buffer.append("\n");
				}
			}
			
			if (writeType != WriteType.SCHEMA_ONLY) {
				for (String tableName : metaData.getTableDefinitions().keySet()) {
					TableDefinition tableDefinition = metaData.getTableDefinition(tableName);
					List<HashMap<String, Object>> tableData = metaData.getTableData(tableName);
					
					List<String> sqlInserts = sqlStatementWriter.buildInsertTableSQL(tableName, tableData, tableDefinition.getColumnDefinitions());				
					
					for (String sqlInsert : sqlInserts) {
						buffer.append(sqlInsert);
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
	
	boolean isValidMetadata(PoijoiMetaData metadata) {
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
