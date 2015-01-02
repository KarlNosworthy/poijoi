package com.karlnosworthy.poijoi.io.reader.xlsx;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.karlnosworthy.poijoi.io.reader.InputStreamReader;

public class XLSXInputStreamReader extends AbstractXLSReader<InputStream> implements
		InputStreamReader {

	@Override
	Workbook getWorkbook(InputStream source) throws Exception {
		return new XSSFWorkbook(source);
	}

}
