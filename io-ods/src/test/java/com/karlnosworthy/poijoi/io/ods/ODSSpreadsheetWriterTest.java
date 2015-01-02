package com.karlnosworthy.poijoi.io.ods;

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

import org.junit.Test;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;

public class ODSSpreadsheetWriterTest {

	@Test
	public void testWrite() throws Exception {
		
		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String,ColumnDefinition>();
		columnDefinitions.put("col1String", new ColumnDefinition("col1String",0, ColumnType.STRING));
		columnDefinitions.put("col2Date", new ColumnDefinition("col2Date",1, ColumnType.DATE));
		columnDefinitions.put("col3Integer", new ColumnDefinition("col3Integer",2, ColumnType.INTEGER_NUMBER));
		columnDefinitions.put("col4Decimal", new ColumnDefinition("col4Decimal",3, ColumnType.DECIMAL_NUMBER));
		
		Map<String, TableDefinition> tableDefinitions = new HashMap<String,TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne", columnDefinitions));
		
		HashMap<String, Object> row1Data = new HashMap<String, Object>();
		row1Data.put("col1String", "hello");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 12, 31, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		row1Data.put("col2Date", cal.getTime());
		row1Data.put("col3Integer", new Integer(19));
		row1Data.put("col4Decimal", new Double("1.5"));
		
		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String,Object>>();
		rowData.add(row1Data);
		
		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		tableData.put("TableOne", rowData);
		
		PoijoiMetaData metaData = new PoijoiMetaData(true, tableDefinitions, tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);
		
		Writer writer = new ODSSpreadsheetWriter();
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.xls");
		file.deleteOnExit();
		
		writer.write(file.getAbsolutePath(), metaData, WriteType.BOTH);
		
		
		// validate contents of the file
		SpreadsheetDocument spreadsheet = SpreadsheetDocument.loadDocument(new FileInputStream(file));
		assertEquals(1, spreadsheet.getTableList().size());
		
		Table table = spreadsheet.getTableByName("TableOne");
		assertNotNull(table);
		assertEquals(2, table.getRowCount());
		spreadsheet.close();
	}
}
