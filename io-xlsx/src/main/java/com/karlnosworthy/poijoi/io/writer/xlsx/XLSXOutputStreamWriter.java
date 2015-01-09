package com.karlnosworthy.poijoi.io.writer.xlsx;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.writer.OutputStreamWriter;

/**
 * An XLSXOutputStreamWriter writes the XLSX file out to an {@link OutputStream}
 * 
 * @author john.bartlett
 *
 */
public class XLSXOutputStreamWriter extends AbstractXLSXWriter<OutputStream> implements
		OutputStreamWriter {

	@Override
	boolean isValidOutput(OutputStream output) {
		if (output == null) {
			return false;
		} else {
			try {
				output.flush();
			} catch (IOException ioException) {
				return false;
			}
		}
		return true;
	}

	@Override
	boolean write(OutputStream output, Workbook workbook) throws Exception {
		workbook.write(output);
		return true;
	}

}
