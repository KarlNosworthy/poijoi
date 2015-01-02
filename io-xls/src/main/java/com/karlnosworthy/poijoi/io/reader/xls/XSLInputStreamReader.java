package com.karlnosworthy.poijoi.io.reader.xls;

import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.InputStreamReader;

public class XSLInputStreamReader extends AbstractXLSReader<InputStream> implements
		InputStreamReader {

	@Override
	Workbook getWorkbook(InputStream source) throws Exception {
		return new HSSFWorkbook(source);
	}

}
