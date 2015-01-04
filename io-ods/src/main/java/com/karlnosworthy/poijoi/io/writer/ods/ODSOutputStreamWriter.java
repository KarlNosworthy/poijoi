package com.karlnosworthy.poijoi.io.writer.ods;

import java.io.OutputStream;

import org.odftoolkit.simple.SpreadsheetDocument;

import com.karlnosworthy.poijoi.io.writer.OutputStreamWriter;

/**
 * A ODSFileWriter writes the ODS file out to an {@link OutputStream}
 * 
 * @author john.bartlett
 *
 */
public class ODSOutputStreamWriter extends AbstractODSWriter<OutputStream>
		implements OutputStreamWriter {

	@Override
	void write(OutputStream output, SpreadsheetDocument spreadsheetDocument)
			throws Exception {
		spreadsheetDocument.save(output);
	}

}
