package com.karlnosworthy.poijoi.io.writer.sqlite;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class SQLiteDatabaseWriterTest {
	
	private SQLiteDatabaseWriter writer;
	
	@Before
	public void onSetup() {
		writer = new SQLiteDatabaseWriter();
	}
	
	@After
	public void onTeardown() {
		writer = null;
	}
	
	@Test
	public void testNullConnection() throws Exception {
		assertFalse(writer.write(null, null, WriteType.BOTH));
	}
	
	@Test
	public void testClosedConnection() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();

		path = "jdbc:sqlite:" + path;

		Connection connection = DriverManager.getConnection(path);
		connection.close();
		
		assertFalse(writer.write(connection, null, WriteType.BOTH));
	}

	/**
	 * Check that passing in null meta-data is handled safely.
	 */
	@Test
	public void testWriteWithNullMetadata() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite").getPath();
			   path = "jdbc:sqlite:" + path;		
		
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(path);
			assertFalse(writer.write(connection, null, WriteType.BOTH));
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	@Test
	public void testWriteWithInvalidMetadata() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite").getPath();
		   path = "jdbc:sqlite:" + path;		
	
		Connection connection = null;
	
		try {
			connection = DriverManager.getConnection(path);
			PoiJoiMetaData metadata = new PoiJoiMetaData(false, null, null);
			assertFalse(writer.write(connection, metadata, WriteType.SCHEMA_ONLY));
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}		
	
	@Test
	public void testSuccessfulWrite() throws Exception {
		List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();
		columnDefinitions.add(new ColumnDefinition("col1String", 0, ColumnType.STRING));
		columnDefinitions.add(new ColumnDefinition("col2Date", 1, ColumnType.DATE));
		columnDefinitions.add(new ColumnDefinition("col3Integer", 2, ColumnType.INTEGER_NUMBER));
		columnDefinitions.add(new ColumnDefinition("col4Decimal", 3, ColumnType.DECIMAL_NUMBER));

		Map<String, TableDefinition> tableDefinitions = new HashMap<String, TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne",
				columnDefinitions));

		HashMap<String, Object> row1Data = new HashMap<String, Object>();
		row1Data.put("col1String", "hello");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 12, 31, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		row1Data.put("col2Date", cal.getTime());
		row1Data.put("col3Integer", new Integer(19));
		row1Data.put("col4Decimal", new Double("1.5"));

		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
		rowData.add(row1Data);

		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		tableData.put("TableOne", rowData);

		PoiJoiMetaData metaData = new PoiJoiMetaData(true, tableDefinitions,
				tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);

		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.sqlite");
		file.deleteOnExit();

		// validate contents of the file
		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ file.getAbsolutePath());

			assertTrue(writer.write(connection, metaData, WriteType.BOTH));

			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet tablesResultSet = databaseMetaData.getTables(null, null,
					null, new String[] { "TABLE" });

			int tableCount = 0;
			while (tablesResultSet.next()) {
				tableCount += 1;
			}
			tablesResultSet.close();

			assertEquals(2, tableCount);

			Statement statement = connection.createStatement();
			ResultSet tableRowCountResultSet = statement
					.executeQuery("select count(*) from TableOne");
			
			assertNotNull(tableRowCountResultSet);
			
			if (tableRowCountResultSet.next()) {
				assertEquals(1, tableRowCountResultSet.getInt(1));
				tableRowCountResultSet.close();
			}

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
}
