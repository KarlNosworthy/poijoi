package com.karlnosworthy.poijoi.io.ods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;

public class ODSSpreadsheetReaderTest {

	@Test
	public void testColumnHeaders() throws Exception {
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();
		ODSSpreadsheetReader reader = new ODSSpreadsheetReader();
		PoijoiMetaData metaData = reader.read(path, false);

		Map<String, TableDefinition> tableDefinitions = metaData
				.getTableDefinitions();
		assertEquals(1, tableDefinitions.size());

		TableDefinition tableDefinition = tableDefinitions.get("Sheet1");
		assertNotNull(tableDefinition);

		assertEquals(4, tableDefinition.getColumnCount());
		Map<String, ColumnDefinition> columnDefinitions = tableDefinition
				.getColumnDefinitions();

		assertTrue(columnDefinitions.containsKey("col1String"));
		assertTrue(columnDefinitions.containsKey("col2Date"));
		assertTrue(columnDefinitions.containsKey("col3Integer"));
		assertTrue(columnDefinitions.containsKey("col4Decimal"));
	}

	@Test
	public void testColumnTypes() throws Exception {

		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();

		ODSSpreadsheetReader reader = new ODSSpreadsheetReader();
		PoijoiMetaData metaData = reader.read(path, false);

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

	@Test
	public void testColumnValues() throws Exception {
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();

		ODSSpreadsheetReader reader = new ODSSpreadsheetReader();
		PoijoiMetaData metaData = reader.read(path, true);

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
		cal.set(Calendar.MILLISECOND,0);
		assertEquals(cal.getTime(), dataRow.get("col2Date"));
		assertEquals(new Integer("2"), dataRow.get("col3Integer"));
		assertEquals(new Double("12.02"), dataRow.get("col4Decimal"));
	}

}
