package com.karlnosworthy.poijoi.io.reader.xlsx;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.FileReader;

/**
 * An XLSXFileReader interacts with an XLSX file using a {@link File}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = "XLSX")
public final class XLSXFileReader extends AbstractXLSReader<File> implements FileReader {

	@Override
	Workbook getWorkbook(File source) throws Exception {
		return new XSSFWorkbook(new FileInputStream(source));
	}

	@Override
	boolean isValidInput(File input) {
		if (input == null || input.isDirectory()) {
			return false;
		}
		return true;
	}
}
