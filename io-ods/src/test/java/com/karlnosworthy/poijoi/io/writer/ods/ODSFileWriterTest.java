package com.karlnosworthy.poijoi.io.writer.ods;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class ODSFileWriterTest {
	
	private ODSFileWriter writer;
	
	
	@Before
	public void onSetup() {
		writer = new ODSFileWriter();
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
		assertFalse(writer.write(null, null, WriteType.SCHEMA_ONLY));
	}

	/**
	 * Check that passing in a file that references a directory is handled safely.
	 */
	@Test
	public void testWriteWithDirectoryNotFile() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		assertFalse(writer.write(new File(javaTmpDir), null, WriteType.SCHEMA_ONLY));
	}
	
	/**
	 * Check that passing in null meta-data is handled safely.
	 */
	@Test
	public void testWriteWithNullMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.ods");
		assertFalse(writer.write(testOutputFile, null, WriteType.SCHEMA_ONLY));
	}

	@Test
	public void testWriteWithInvalidMetadata() throws Exception {
		String javaTmpDir = System.getProperty("java.io.tmpdir");
		File testOutputFile = new File(javaTmpDir, "temp_spreadsheet.ods");
		PoiJoiMetaData metadata = new PoiJoiMetaData(false, null, null);
		assertFalse(writer.write(testOutputFile, metadata, WriteType.SCHEMA_ONLY));
	}

	/**
	 * Based on a mocked up Table structure and data set make sure the writer
	 * correctly outputs a valid ODS file
	 */
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

		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir, "test.xls");
		file.deleteOnExit();

		assertTrue(writer.write(file, metaData, WriteType.BOTH));

		// validate contents of the file
		SpreadsheetDocument spreadsheet = SpreadsheetDocument
				.loadDocument(new FileInputStream(file));
		assertEquals(1, spreadsheet.getTableList().size());

		Table table = spreadsheet.getTableByName("TableOne");
		assertNotNull(table);
		assertEquals(2, table.getRowCount());
		spreadsheet.close();
	}
}
