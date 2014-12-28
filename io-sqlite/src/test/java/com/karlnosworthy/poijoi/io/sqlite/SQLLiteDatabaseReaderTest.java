package com.karlnosworthy.poijoi.io.sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;

public class SQLLiteDatabaseReaderTest {
	
	@Test
	public void testColumnHeaders() throws Exception {
		
		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();
		
		path = "jdbc:sqlite:" + path;
		
		SQLiteDatabaseReader reader = new SQLiteDatabaseReader();
		PoijoiMetaData metaData = reader.read(path, false);

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
	}
	
	@Test
	public void testColumnValues() throws Exception {
		String path = getClass().getClassLoader().getResource("test.sqlite")
				.getPath();
		
		path = "jdbc:sqlite:" + path;
		
		SQLiteDatabaseReader reader = new SQLiteDatabaseReader();
		PoijoiMetaData metaData = reader.read(path, true);
		
		// pull back sheet1 data
		List<HashMap<String, String>> tableData = metaData
				.getTableData("Sheet1");
		assertEquals(2, tableData.size());

		Map<String, String> dataRow = tableData.get(1);
		// check the column values
		assertEquals("string2", dataRow.get("col1String"));
		// Date is defaulting to DateTime object
		assertEquals("2014-12-17 00:00:00.000", dataRow.get("col2Date"));
		assertEquals("2", dataRow.get("col3Integer"));
		assertEquals("12.02", dataRow.get("col4Decimal"));
	}	
	
}
