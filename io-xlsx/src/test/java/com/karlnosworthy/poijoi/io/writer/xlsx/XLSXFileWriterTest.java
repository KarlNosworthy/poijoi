package com.karlnosworthy.poijoi.io.writer.xlsx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class XLSXFileWriterTest {

	private XLSXFileWriter writer;
	
	@Before
	public void onSetup() {
		writer = new XLSXFileWriter();
	}
	
	@After
	public void onTeardown() {
		writer = null;
	}
	
	/**
	 * Check that passing in a null file is handled safety.
	 */
	@Test
	public void testWriteWithNullFile() throws Exception {
		writer.write(null, null, WriteType.SCHEMA_ONLY);
	}

	/**
	 * Check that passing in a file that references a directory is handled safely.
	 */
	@Test
	public void testWriteWithDirectoryNotFile() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		writer.write(new File(javaTmpDir), null, WriteType.SCHEMA_ONLY);
	}
	
	/**
	 * Check that passing in null meta-data is handled safely.
	 */
	@Test
	public void testWriteWithNullMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.ods");
		writer.write(testOutputFile, null, WriteType.SCHEMA_ONLY);
	}

	@Test
	public void testWriteWithInvalidMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.ods");
		PoijoiMetaData metadata = new PoijoiMetaData(false, null, null);
		writer.write(testOutputFile, metadata, WriteType.SCHEMA_ONLY);
	}		

	/**
	 * Based on a mocked up Table structure and data set make sure the writer
	 * correctly outputs a valid XLSX file
	 */
	@Test
	public void testSuccessfulWrite() throws Exception {

		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String, ColumnDefinition>();
		columnDefinitions.put("col1String", new ColumnDefinition("col1String",
				0, ColumnType.STRING));
		columnDefinitions.put("col2Date", new ColumnDefinition("col2Date", 1,
				ColumnType.DATE));
		columnDefinitions.put("col3Integer", new ColumnDefinition(
				"col3Integer", 2, ColumnType.INTEGER_NUMBER));
		columnDefinitions.put("col4Decimal", new ColumnDefinition(
				"col4Decimal", 3, ColumnType.DECIMAL_NUMBER));

		Map<String, TableDefinition> tableDefinitions = new HashMap<String, TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne",
				columnDefinitions));

		HashMap<String, Object> row1Data = new HashMap<String, Object>();
		row1Data.put("col1String", "hello");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 12, 31);
		row1Data.put("col2Date", cal.getTime());
		row1Data.put("col3Integer", new Integer("19"));
		row1Data.put("col4Decimal", new Double("1.5"));

		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
		rowData.add(row1Data);

		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		tableData.put("TableOne", rowData);

		PoijoiMetaData metaData = new PoijoiMetaData(true, tableDefinitions,
				tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);

		XLSXFileWriter writer = new XLSXFileWriter();
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.xlsx");
		file.deleteOnExit();

		writer.write(file, metaData, WriteType.BOTH);

		// validate contents of the file
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
		assertEquals(1, wb.getNumberOfSheets());

		XSSFSheet sheet = wb.getSheet("TableOne");
		assertNotNull(sheet);
		assertEquals(1, sheet.getLastRowNum());
	}
}
