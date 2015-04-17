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

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
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
		PoiJoiMetaData metadata = reader.read(null, false);
		assertNull(metadata);
	}
	
	@Test
	public void testClosedConnection() throws Exception {
		String path = getClass().getClassLoader().getResource("test.mdb")
				.getPath();

		path = "jdbc:ucanaccess://" + path;

		Connection connection = DriverManager.getConnection(path);
		connection.close();
		
		PoiJoiMetaData metadata = reader.read(connection, false);
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
			PoiJoiMetaData metaData = reader.read(connection, false);

			Map<String, TableDefinition> tableDefinitions = metaData
					.getTableDefinitions();
			assertEquals(1, tableDefinitions.size());
			
			TableDefinition tableDefinition = tableDefinitions.get("TABLEONE");
			assertNotNull(tableDefinition);

			assertEquals(5, tableDefinition.getColumnCount());
			assertTrue(tableDefinition.containsDefinitionForColumn("ID"));
			assertTrue(tableDefinition.containsDefinitionForColumn("COL1STRING"));
			assertTrue(tableDefinition.containsDefinitionForColumn("COL2DATE"));
			assertTrue(tableDefinition.containsDefinitionForColumn("COL3INTEGER"));
			assertTrue(tableDefinition.containsDefinitionForColumn("COL4DECIMAL"));

		} finally {
			if (connection != null) {
				connection.close();
			}
		}

	}	
}
