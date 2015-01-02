package com.karlnosworthy.poijoi.io.writer.xls;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.FileWriter;

@SupportsFormat(type = FormatType.XLS)
public class XLSFileWriter extends AbstractXLSWriter<File> implements
		FileWriter {

	@Override
	void write(File output, Workbook workbook) throws Exception {
		FileOutputStream fos = new FileOutputStream(output);
		workbook.write(fos);
		fos.close();
	}

}