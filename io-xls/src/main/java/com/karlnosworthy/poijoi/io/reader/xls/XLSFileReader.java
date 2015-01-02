package com.karlnosworthy.poijoi.io.reader.xls;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.FileReader;
import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;

@SupportsFormat(type = FormatType.XLS)
public final class XLSFileReader extends AbstractXLSReader<File> implements FileReader {

	@Override
	Workbook getWorkbook(File source) throws Exception {
		return new HSSFWorkbook(new FileInputStream(source));
	}
	
}
