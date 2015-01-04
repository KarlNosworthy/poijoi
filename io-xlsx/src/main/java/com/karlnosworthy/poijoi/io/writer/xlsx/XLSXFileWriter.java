package com.karlnosworthy.poijoi.io.writer.xlsx;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.FileWriter;

/**
 * An XLSXFileWriter writes the XLSX file out to an {@link File}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = FormatType.XLSX)
public class XLSXFileWriter extends AbstractXLSXWriter<File> implements
		FileWriter {

	@Override
	void write(File output, Workbook workbook) throws Exception {
		FileOutputStream fos = new FileOutputStream(output);
		workbook.write(fos);
		fos.close();
	}

}
