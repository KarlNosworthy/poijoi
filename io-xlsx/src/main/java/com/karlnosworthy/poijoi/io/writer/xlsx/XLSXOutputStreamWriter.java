package com.karlnosworthy.poijoi.io.writer.xlsx;

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
	void write(OutputStream output, Workbook workbook) throws Exception {
		workbook.write(output);
	}

}
