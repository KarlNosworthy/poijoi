package com.karlnosworthy.poijoi.io.writer.xls;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.FileWriter;

/**
 * A XLSFileWriter writes the XLS file using an {@link File}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = FormatType.XLS)
public class XLSFileWriter extends AbstractXLSWriter<File> implements
		FileWriter {

	@Override
	boolean isValidOutput(File output) {
		if (output == null || output.isDirectory()) {
			return false;
		}
		return true;
	}
	
	@Override
	boolean write(File output, Workbook workbook) throws Exception {
		FileOutputStream fos = new FileOutputStream(output);
		workbook.write(fos);
		fos.close();
		return true;
	}

}
