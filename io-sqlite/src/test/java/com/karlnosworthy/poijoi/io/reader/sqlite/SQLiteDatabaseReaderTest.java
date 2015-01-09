package com.karlnosworthy.poijoi.io.reader.sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class SQLiteDatabaseReaderTest {
	
	private SQLiteDatabaseReader reader;
	
	
	@Before
	public void onSetup() {
		reader = new SQLiteDatabaseReader();
	}
	
	@After
	public void onTeardown() {
		reader = null;
	}
	
	@Test
	public void testNullConnection() throws Exception {
		PoijoiMetaData metadata = reader.read(null, false);
		assertNull(metadata);
	}
	
	@Test
	public void testClosedConnection() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();

		path = "jdbc:sqlite:" + path;

		Connection connection = DriverManager.getConnection(path);
		connection.close();
		
		PoijoiMetaData metadata = reader.read(connection, false);
		assertNull(metadata);
	}

	/**
	 * Test that the column names are correctly read using the headers
	 */
	@Test
	public void testColumnHeaders() throws Exception {

		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();

		path = "jdbc:sqlite:" + path;

		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(path);
			PoijoiMetaData metaData = reader.read(connection, false);

			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			assertEquals(1, tableDefinitions.size());

			TableDefinition tableDefinition = tableDefinitions.get("Sheet1");
			assertNotNull(tableDefinition);

			assertEquals(5, tableDefinition.getColumnCount());
			Map<String, ColumnDefinition> columnDefinitions = tableDefinition
					.getColumnDefinitions();

			assertTrue(columnDefinitions.containsKey("id"));
			assertTrue(columnDefinitions.containsKey("col1String"));
			assertTrue(columnDefinitions.containsKey("col2Date"));
			assertTrue(columnDefinitions.containsKey("col3Integer"));
			assertTrue(columnDefinitions.containsKey("col4Decimal"));

		} finally {
			if (connection != null) {
				connection.close();
			}
		}

	}

	/**
	 * Test that the column values come through as expected and are the correct
	 * types
	 */
	@Test
	public void testColumnValues() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();

		path = "jdbc:sqlite:" + path;

		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(path);
			PoijoiMetaData metaData = reader.read(connection, true);

			// pull back sheet1 data
			List<HashMap<String, Object>> tableData = metaData
					.getTableData("Sheet1");

			assertEquals(2, tableData.size());

			Map<String, Object> dataRow = tableData.get(1);
			// check the column values
			assertEquals("string2", dataRow.get("col1String"));
			// Date is defaulting to DateTime object
			Calendar cal = Calendar.getInstance();
			cal.set(2014, 11, 17, 0, 0, 0);
			cal.set(Calendar.MILLISECOND, 0);
			assertEquals(cal.getTime(), dataRow.get("col2Date"));
			assertEquals(new Integer("2"), dataRow.get("col3Integer"));
			assertEquals(new Double("12.02"), dataRow.get("col4Decimal"));
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
}
