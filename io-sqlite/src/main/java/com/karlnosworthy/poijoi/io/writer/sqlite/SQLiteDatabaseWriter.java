package com.karlnosworthy.poijoi.io.writer.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import com.karlnosworthy.poijoi.PoiJoiOptions;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.OptionAware;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.sqlite.SQLiteDatabaseReader;
import com.karlnosworthy.poijoi.io.writer.JDBCConnectionWriter;
import com.karlnosworthy.poijoi.jdbc.JDBCDatabaseCreator;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;

/**
 * Write an SQLite Database from the contents of a {@link PoijoiMetaData}.
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = FormatType.SQLITE)
public class SQLiteDatabaseWriter implements JDBCConnectionWriter, OptionAware {

	private static final Logger logger = LoggerFactory
			.getLogger(SQLiteDatabaseReader.class);

	private PoiJoiOptions options;

	/**
	 * Writes an SQLite Database (and optionally the data) based on the contents
	 * of a {@link PoijoiMetaData}.
	 * 
	 * @param connection
	 *            The connection to the SQLite Database
	 * @param metaData
	 *            A {@link PoijoiMetaData} holding the table structures and
	 *            optionally the table data
	 * @param writeType
	 *            Control over what gets written
	 */
	@Override
	public final void write(Connection connection, PoijoiMetaData metaData,
			WriteType writeType) throws Exception {
		Class.forName("org.sqlite.JDBC");
		JDBCDatabaseCreator databaseCreator = new JDBCDatabaseCreator();

		if (options != null && options.hasValue("--version")) {
			Integer versionNumber = Integer.parseInt(options
					.getValue("--version"));
			setVersionNumber(connection, versionNumber);
		}

		if (writeType != WriteType.DATA_ONLY) {
			int numberOfTablesCreated = databaseCreator.createTables(
					connection, metaData);
			logger.info("Created {} tables....", numberOfTablesCreated);
		}

		if (writeType != WriteType.SCHEMA_ONLY) {
			int inserts = databaseCreator.writeData(connection, metaData);
			logger.info("Inserted {} row(s)....", inserts);
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
}
