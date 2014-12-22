package com.karlnosworthy.poijoi.io.xls;

import org.junit.Test;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;
import com.karlnosworthy.poijoi.io.ods.ODSSpreadsheetReader;

public class XLSSpreadsheetWriterTest {

	@Test
	public void testWrite() throws Exception {
		
		// read in ODS file
		String path = getClass().getClassLoader().getResource("test1.ods")
				.getPath();
		ODSSpreadsheetReader reader = new ODSSpreadsheetReader();
		PoijoiMetaData metaData = reader.read(path, true);

		// write that ODS into a XLS
		Writer writer = new XLSSpreadsheetWriter();
		writer.write("/home/barty/test.xls", metaData, WriteType.BOTH);
		
	}
	
}
