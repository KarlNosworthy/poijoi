package com.karlnosworthy.poijoi.io.writer.ods;

import java.io.IOException;
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
	void write(OutputStream output, SpreadsheetDocument spreadsheetDocument)
			throws Exception {
		spreadsheetDocument.save(output);
	}

}
