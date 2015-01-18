package com.karlnosworthy.poijoi.io.reader.mdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.io.reader.mdb.MDBDatabaseReader;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class MDBDatabaseReaderTest {

	private MDBDatabaseReader reader;
	
	@Before
	public void setup() throws Exception {
		reader = new MDBDatabaseReader();
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
		String path = getClass().getClassLoader().getResource("test.mdb")
				.getPath();

		path = "jdbc:ucanaccess://" + path;

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

		String path = getClass().getClassLoader().getResource("test.mdb")
				.getPath();

		path = "jdbc:ucanaccess://" + path;

		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(path);
			PoijoiMetaData metaData = reader.read(connection, false);

			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			assertEquals(1, tableDefinitions.size());
			
			TableDefinition tableDefinition = tableDefinitions.get("TABLEONE");
			assertNotNull(tableDefinition);

			assertEquals(5, tableDefinition.getColumnCount());
			Map<String, ColumnDefinition> columnDefinitions = tableDefinition
					.getColumnDefinitions();

			assertTrue(columnDefinitions.containsKey("ID"));
			assertTrue(columnDefinitions.containsKey("COL1STRING"));
			assertTrue(columnDefinitions.containsKey("COL2DATE"));
			assertTrue(columnDefinitions.containsKey("COL3INTEGER"));
			assertTrue(columnDefinitions.containsKey("COL4DECIMAL"));

		} finally {
			if (connection != null) {
				connection.close();
			}
		}

	}	
}
