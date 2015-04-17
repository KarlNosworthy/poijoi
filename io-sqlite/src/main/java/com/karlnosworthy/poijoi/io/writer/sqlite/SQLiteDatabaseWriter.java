package com.karlnosworthy.poijoi.io.writer.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import com.karlnosworthy.poijoi.PoiJoiOptions;
import com.karlnosworthy.poijoi.io.OptionAware;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.sqlite.SQLiteDatabaseReader;
import com.karlnosworthy.poijoi.io.writer.JDBCConnectionWriter;
import com.karlnosworthy.poijoi.jdbc.JDBCDatabaseCreator;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;

/**
 * Write an SQLite Database from the contents of a {@link PoiJoiMetaData}.
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = "SQLITE")
public class SQLiteDatabaseWriter implements JDBCConnectionWriter, OptionAware {

	private static final Logger logger = LoggerFactory
			.getLogger(SQLiteDatabaseReader.class);

	private PoiJoiOptions options;
	
	/**
	 * Writes an SQLite Database (and optionally the data) based on the contents
	 * of a {@link PoiJoiMetaData}.
	 * 
	 * @param connection
	 *            The connection to the SQLite Database
	 * @param metaData
	 *            A {@link PoiJoiMetaData} holding the table structures and
	 *            optionally the table data
	 * @param writeType
	 *            Control over what gets written
	 *            
	 * @return True if the write was successful otherwise false
	 */
	@Override
	public final boolean write(Connection connection, PoiJoiMetaData metaData,
			WriteType writeType) throws Exception {
		
		if (isValidConnection(connection) && isValidMetadata(metaData)) {
			
			Class.forName("org.sqlite.JDBC");
			JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator(connection);
	
			if (options != null && options.hasValue("--version")) {
				Integer versionNumber = Integer.parseInt(options
						.getValue("--version"));
				setVersionNumber(connection, versionNumber);
			}
			
			return databaseCreator.create(metaData, writeType);
		} else {
			return false;
		}
	}

	private boolean setVersionNumber(Connection connection,
			Integer versionNumber) {
		SQLiteConfig config = new SQLiteConfig();
		config.setUserVersion(versionNumber);
		try {
			config.apply(connection);
			logger.info("Just set the version number to '{}'", versionNumber);
			return true;
		} catch (SQLException e1) {
			logger.warn(e1.getMessage(), e1);
		}
		return false;
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
