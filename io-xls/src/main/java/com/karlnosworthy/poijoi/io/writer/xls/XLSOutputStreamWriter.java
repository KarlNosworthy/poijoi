package com.karlnosworthy.poijoi.io.writer.xls;

import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.karlnosworthy.poijoi.io.writer.OutputStreamWriter;

public class XLSOutputStreamWriter extends AbstractXLSWriter<OutputStream> implements OutputStreamWriter {

	@Override
	void write(OutputStream output, Workbook workbook) throws Exception {
		workbook.write(output);
	}

}
