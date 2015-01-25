package com.karlnosworthy.poijoi.io.writer.xls;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.OutputStreamWriter;

/**
 * A XLSFileWriter writes the XLS file using an {@link OutputStream}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = "XLS")
public class XLSOutputStreamWriter extends AbstractXLSWriter<OutputStream> implements OutputStreamWriter {

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
