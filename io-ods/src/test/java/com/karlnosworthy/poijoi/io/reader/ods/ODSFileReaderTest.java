package com.karlnosworthy.poijoi.io.reader.ods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class ODSFileReaderTest {

	/**
	 * Check that passing in a null file is handled safety.
	 */
	@Test
	public void testReaderWithNullFile() throws Exception {
		ODSFileReader reader = new ODSFileReader();
		PoiJoiMetaData metaData = reader.read(null, true);
		assertTrue(metaData == null);
	}

	/**
	 * Check that passing in a file that references a directory is handled safely.
	 */
	@Test
	public void testReaderWithDirectoryNotFile() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		ODSFileReader reader = new ODSFileReader();
		PoiJoiMetaData metaData = reader.read(new File(javaTmpDir), true);
		assertTrue(metaData == null);
	}

	/**
	 * Test that the column names are correctly read using the headers
	 */
	@Test
	public void testColumnHeaders() throws Exception {
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();
		ODSFileReader reader = new ODSFileReader();
		PoiJoiMetaData metaData = reader.read(new File(path), false);

		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();
		assertEquals(1, tableDefinitions.size());

		TableDefinition tableDefinition = tableDefinitions.get("Sheet1");
		assertNotNull(tableDefinition);

		assertEquals(4, tableDefinition.getColumnCount());
		assertTrue(tableDefinition.containsDefinitionForColumn("col1String"));
		assertTrue(tableDefinition.containsDefinitionForColumn("col2Date"));
		assertTrue(tableDefinition.containsDefinitionForColumn("col3Integer"));
		assertTrue(tableDefinition.containsDefinitionForColumn("col4Decimal"));
	}

	/**
	 * Check that the mappings for the column types work as expected
	 */
	@Test
	public void testColumnTypes() throws Exception {

		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();

		ODSFileReader reader = new ODSFileReader();
		PoiJoiMetaData metaData = reader.read(new File(path), false);

		TableDefinition tableDefinition = metaData.getTableDefinition("Sheet1");
		assertNotNull(tableDefinition);

		assertEquals(ColumnType.STRING,
				tableDefinition.getColumnDefinition("col1String")
						.getColumnType());
		assertEquals(ColumnType.DATE,
				tableDefinition.getColumnDefinition("col2Date").getColumnType());
		assertEquals(ColumnType.INTEGER_NUMBER, tableDefinition
				.getColumnDefinition("col3Integer").getColumnType());
		assertEquals(ColumnType.DECIMAL_NUMBER, tableDefinition
				.getColumnDefinition("col4Decimal").getColumnType());
	}

	/**
	 * Test that the column values come through as expected and are the correct
	 * types
	 */
	@Test
	public void testColumnValues() throws Exception {
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();

		ODSFileReader reader = new ODSFileReader();
		PoiJoiMetaData metaData = reader.read(new File(path), true);

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
	}
}
