package com.karlnosworthy.poijoi.io.xlsx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.karlnosworthy.poijoi.core.model.ColumnDefinition;
import com.karlnosworthy.poijoi.core.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.core.model.TableDefinition;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;

public class XLSXSpreadsheetWriterTest {

	@Test
	public void testWrite() throws Exception {
		
		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String,ColumnDefinition>();
		columnDefinitions.put("col1String", new ColumnDefinition("col1String",0, ColumnType.STRING));
		columnDefinitions.put("col2Date", new ColumnDefinition("col2Date",1, ColumnType.DATE));
		columnDefinitions.put("col3Integer", new ColumnDefinition("col3Integer",2, ColumnType.INTEGER_NUMBER));
		columnDefinitions.put("col4Decimal", new ColumnDefinition("col4Decimal",3, ColumnType.DECIMAL_NUMBER));
		
		Map<String, TableDefinition> tableDefinitions = new HashMap<String,TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne", columnDefinitions));
		
		HashMap<String,String> row1Data = new HashMap<String,String>();
		row1Data.put("col1String", "hello");
		row1Data.put("col2Date", "31/12/2014");
		row1Data.put("col3Integer", "19");
		row1Data.put("col4Decimal", "1.5");
		
		List<HashMap<String,String>> rowData = new ArrayList<HashMap<String,String>>();
		rowData.add(row1Data);
		
		Map<String, List<HashMap<String, String>>> tableData = new HashMap<String, List<HashMap<String, String>>>();
		tableData.put("TableOne", rowData);
		
		PoijoiMetaData metaData = new PoijoiMetaData(true, tableDefinitions, tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);
		
		Writer writer = new XLSXSpreadsheetWriter();
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.xlsx");
		file.deleteOnExit();
		
		writer.write(file.getAbsolutePath(), metaData, WriteType.BOTH);
		
		
		// validate contents of the file
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
		assertEquals(1, wb.getNumberOfSheets());
		
		XSSFSheet sheet = wb.getSheet("TableOne");
		assertNotNull(sheet);
		assertEquals(1, sheet.getLastRowNum());
	}	
}
