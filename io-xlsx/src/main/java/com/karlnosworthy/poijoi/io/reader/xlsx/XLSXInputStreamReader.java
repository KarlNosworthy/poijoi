package com.karlnosworthy.poijoi.io.reader.xlsx;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.InputStreamReader;

/**
 * An XLSXInputStreamReader interacts with an XLSX file using a
 * {@link InputStream}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = "XLSX")
public class XLSXInputStreamReader extends AbstractXLSReader<InputStream>
		implements InputStreamReader {

	@Override
	Workbook getWorkbook(InputStream source) throws Exception {
		return new XSSFWorkbook(source);
	}

	@Override
	boolean isValidInput(InputStream input) {
		if (input == null) {
			return false;
		} else {
			try {
				input.available();
			} catch (IOException ioException) {
				return false;
			}
		}
		return true;
	}
}
