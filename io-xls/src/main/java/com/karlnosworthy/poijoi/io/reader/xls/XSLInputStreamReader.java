package com.karlnosworthy.poijoi.io.reader.xls;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.reader.InputStreamReader;

/**
 * An XSLInputStreamReader interacts with an XLS file using a {@link InputStream}
 * 
 * @author john.bartlett
 *
 */
public class XSLInputStreamReader extends AbstractXLSReader<InputStream> implements
		InputStreamReader {

	@Override
	Workbook getWorkbook(InputStream source) throws Exception {
		return new HSSFWorkbook(source);
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
