package com.karlnosworthy.poijoi.io.writer.xls;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class XLSOutputStreamWriterTest {

	private XLSOutputStreamWriter outputStreamWriter;
	
	@Before
	public void onSetup() {
		outputStreamWriter = new XLSOutputStreamWriter();
	}
	
	@After
	public void onTeardown() {
		outputStreamWriter = null;
	}

	/**
	 * Check that passing in a null stream is handled safety.
	 */
	@Test
	public void testWriteWithNullOutputStream() throws Exception {
		assertFalse(outputStreamWriter.write(null, null, WriteType.BOTH));
	}
	
	@Test
	public void testWriteWithClosedInputStream() throws Exception {
		
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File tempOutputFile = new File(javaTmpDir,"temp_spreadsheet.xls");
		OutputStream outputStream = new FileOutputStream(tempOutputFile);
		outputStream.close();
		
		assertFalse(outputStreamWriter.write(outputStream, null, WriteType.BOTH));
	}
	
	@Test
	public void testWriteWithNullMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.xls");

		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(testOutputFile);
			assertFalse(outputStreamWriter.write(outputStream, null, WriteType.SCHEMA_ONLY));
		} finally {
			outputStream.close();
		}
	}
	
	@Test
	public void testWriteWithInvalidMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.xls");

		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(testOutputFile);
			PoijoiMetaData metadata = new PoijoiMetaData(false, null, null);
			assertFalse(outputStreamWriter.write(outputStream, metadata, WriteType.SCHEMA_ONLY));
		} finally {
			outputStream.close();
		}
	}
	
	/**
	 * Based on a mocked up Table structure and data set make sure the writer
	 * correctly outputs a valid ODS file
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
		cal.set(2014, 12, 31, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		row1Data.put("col2Date", cal.getTime());
		row1Data.put("col3Integer", new Integer(19));
		row1Data.put("col4Decimal", new Double("1.5"));

		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
		rowData.add(row1Data);

		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		tableData.put("TableOne", rowData);

		PoijoiMetaData metaData = new PoijoiMetaData(true, tableDefinitions,
				tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);


		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testFile = new File(javaTmpDir, "temp_spreadsheet.xls");
		testFile.deleteOnExit();

		OutputStream outputStream = new FileOutputStream(testFile);
		assertTrue(outputStreamWriter.write(outputStream, metaData, WriteType.BOTH));
		outputStream.close();

		// validate contents of the file
		Workbook workbook = new HSSFWorkbook(new FileInputStream(testFile));
		
		assertEquals(1, workbook.getNumberOfSheets());

		Sheet table = workbook.getSheet("TableOne");
		assertNotNull(table);
		assertEquals(1, table.getLastRowNum());
	}	
}
