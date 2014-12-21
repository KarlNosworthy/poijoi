package com.karlnosworthy.poijoi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.karlnosworthy.poijoi.core.SpreadsheetScanner;
import com.karlnosworthy.poijoi.core.SpreadsheetScanner.ColumnType;


public class XLSSpreadsheetScannerTest {
	
	@Test
	public void testColumnHeaders() throws Exception {
		
		File testFile = new File(getClass().getClassLoader().getResource("test1.xls").getFile());
		
		SpreadsheetScanner scanner = new SpreadsheetScanner(testFile);
		Set<String> columnNames = scanner.getColumnNames("Sheet1");
		assertEquals(4,columnNames.size());
		//check the column names
		boolean checkedCol1 = false;
		boolean checkedCol2 = false;
		boolean checkedCol3 = false;
		boolean checkedCol4 = false;
		Iterator<String> columnIterator = columnNames.iterator();
		while (columnIterator.hasNext()) {
			String columnName = columnIterator.next();
			if (columnName.equalsIgnoreCase("col1String")) {
				checkedCol1 = true;
			}
			if (columnName.equalsIgnoreCase("col2Date")) {
				checkedCol2 = true;
			}
			if (columnName.equalsIgnoreCase("col3Integer")) {
				checkedCol3 = true;
			}
			if (columnName.equalsIgnoreCase("col4Decimal")) {
				checkedCol4 = true;
			}
		}
		assertTrue(checkedCol1);
		assertTrue(checkedCol2);
		assertTrue(checkedCol3);
		assertTrue(checkedCol4);
	}

	@Test
	public void testColumnTypes() throws Exception {
		File testFile = new File(getClass().getClassLoader().getResource("test1.xls").getFile());
		
		SpreadsheetScanner scanner = new SpreadsheetScanner(testFile);
		Map<String, ColumnType> columns = scanner.getColumnNamesAndTypes("Sheet1");
		assertEquals(4,columns.size());
		//check the column types
		boolean checkedCol1 = false;
		boolean checkedCol2 = false;
		boolean checkedCol3 = false;
		boolean checkedCol4 = false;
		for (String columnName : columns.keySet()) {
			if (columnName.equalsIgnoreCase("col1String")) {
				if (columns.get(columnName).equals(ColumnType.STRING)) {
					checkedCol1 = true;
				}
			}
			if (columnName.equalsIgnoreCase("col2Date")) {
				if (columns.get(columnName).equals(ColumnType.DATE)) {
					checkedCol2 = true;
				}
			}
			if (columnName.equalsIgnoreCase("col3Integer")) {
				if (columns.get(columnName).equals(ColumnType.INTEGER_NUMBER)) {
					checkedCol3 = true;
				}
			}
			if (columnName.equalsIgnoreCase("col4Decimal")) {
				if (columns.get(columnName).equals(ColumnType.DECIMAL_NUMBER)) {
					checkedCol4 = true;
				}
			}
		}
		assertTrue(checkedCol1);
		assertTrue(checkedCol2);
		assertTrue(checkedCol3);
		assertTrue(checkedCol4);
	}

	@Test
	public void testColumnValues() throws Exception {
		File testFile = new File(getClass().getClassLoader().getResource("test1.xls").getFile());
		
		SpreadsheetScanner scanner = new SpreadsheetScanner(testFile);
		List<HashMap<String, String>> tableData = scanner.getTableData("Sheet1");
		assertEquals(2,tableData.size());
		
		Map<String,String> dataRow = tableData.get(1);
		//check the column values
		assertEquals("string2",dataRow.get("col1String"));
		//Date is defaulting to DateTime object
		assertEquals("2014-12-17 00:00:00.000",dataRow.get("col2Date"));
		assertEquals("2",dataRow.get("col3Integer"));
		assertEquals("12.02",dataRow.get("col4Decimal"));
	}

}
