package com.karlnosworthy.poijoi.io.xls;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;
//import com.karlnosworthy.poijoi.io.ods.ODSSpreadsheetReader;

public class XLSSpreadsheetWriterTest {

	@Test
	public void testWrite() throws Exception {
/*		
		// read in ODS file
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();
		ODSSpreadsheetReader reader = new ODSSpreadsheetReader();
		PoijoiMetaData metaData = reader.read(path, true);

		// write that ODS into a XLS
		Writer writer = new XLSSpreadsheetWriter();
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.xls");
		writer.write(file.getAbsolutePath(), metaData, WriteType.BOTH);
		
		// validate contents of the file
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
		assertEquals(1, wb.getNumberOfSheets());
		
		HSSFSheet sheet = wb.getSheet("Sheet1");
		assertNotNull(sheet);
		
		assertEquals(2, sheet.getLastRowNum());
*/		
		
		
	}
	
}
