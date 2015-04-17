package com.karlnosworthy.poijoi.io.writer.mdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.PoiJoiOptions;
import com.karlnosworthy.poijoi.io.OptionAware;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.JDBCConnectionWriter;
import com.karlnosworthy.poijoi.jdbc.JDBCDatabaseCreator;
import com.karlnosworthy.poijoi.jdbc.SQLStatementCreator;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

@SupportsFormat(type = "MDB")
public class MDBDatabaseWriter implements JDBCConnectionWriter, OptionAware {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MDBDatabaseWriter.class);

	private PoiJoiOptions options;


	@Override
	public boolean write(Connection connection, PoiJoiMetaData metadata, WriteType writeType) throws Exception {
		if (isValidConnection(connection) && isValidMetadata(metadata)) {
			
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
			JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator(new MDBSQLStatementCreator(), connection);
			databaseCreator.create(metadata, writeType);
//			JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator(connection);
//			databaseCreator.create(metadata, writeType);
			return true;
		} else {
			return false;
		}		
	}

	public void setOptions(PoiJoiOptions options) {
		this.options = options;
	}
	
	@Override
	public boolean isValidConnection(Connection connection) {
		if (connection == null) {
			return false;
		} else {
			try {
				if (connection.isClosed() || connection.isReadOnly()) {
					return false;
				}
			} catch (SQLException sqlException) {
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

class MDBSQLStatementCreator extends SQLStatementCreator {
	
	@Override
	public String buildCreateTableStatement(TableDefinition tableDefinition) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("CREATE TABLE ");
		builder.append(tableDefinition.getTableName());
		builder.append(" (");
		builder.append("id COUNTER");

		for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
			builder.append(",");

			if (columnDefinition.getColumnName().indexOf(".") >= 0) {
				builder.append(columnDefinition.getColumnName().replace('.', '_'));
			} else {
				builder.append(columnDefinition.getColumnName());
			}

			ColumnType columnType = columnDefinition.getColumnType();

			switch (columnType) {
			case STRING:
				builder.append(" TEXT");
				break;
			case INTEGER_NUMBER:
				builder.append(" LONG");
				break;
			case DECIMAL_NUMBER:
				builder.append(" DOUBLE");
				break;
			case DATE:
				builder.append(" DATETIME");
				break;
			}
		}
		builder.append(");");
		
		String sqlString = builder.toString();
		return sqlString;
	}

	public String buildInsertTableStatement(TableDefinition tableDefinition, Map<String, Object> dataToInsert) {

		List<String> insertSqlStrings = new ArrayList<String>();

		StringBuilder builder = new StringBuilder();

		builder.append("INSERT INTO ");
		builder.append(tableDefinition.getTableName());
		builder.append(" (");

		boolean first = true;
		for (String columnName : dataToInsert.keySet()) {
			if (!columnName.endsWith(".id")) {
				if (first) {
					first = false;
				} else {
					builder.append(",");
				}
				builder.append("");
				builder.append(columnName);
				builder.append("");
			}
		}

		builder.append(")");
		builder.append(" VALUES (");

		first = true;

		ColumnDefinition columnDefinition = null;

		for (String columnName : dataToInsert.keySet()) {

			columnDefinition = tableDefinition.getColumnDefinition(columnName);

			if (!columnName.endsWith(".id")) {
				if (first) {
					first = false;
				} else {
					builder.append(",");
				}

				Object val = dataToInsert.get(columnName);
				if (columnDefinition.getColumnType() == ColumnType.DATE) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss.SSS");
					builder.append("'");
					builder.append(dateFormat.format(val));
					builder.append("'");
				} else if (columnDefinition.getColumnType() == ColumnType.STRING) {
					builder.append("'");
					builder.append(val);
					builder.append("'");
				} else {
					builder.append(dataToInsert.get(columnName));
				}
			}
		}
		builder.append(");");

		return builder.toString();
	}
}
