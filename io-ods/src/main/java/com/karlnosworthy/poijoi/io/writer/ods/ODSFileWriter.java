package com.karlnosworthy.poijoi.io.writer.ods;

import java.io.File;

import org.odftoolkit.simple.SpreadsheetDocument;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.FileWriter;

/**
 * A ODSFileWriter writes the ODS file out to a {@link File}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = FormatType.ODS)
public class ODSFileWriter extends AbstractODSWriter<File> implements FileWriter {

	@Override
	boolean isValidOutput(File output) {
		if (output == null || output.isDirectory()) {
			return false;
		}
		return true;
	}
	
	@Override
	boolean write(File output, SpreadsheetDocument spreadsheetDocument) throws Exception {
		spreadsheetDocument.save(output);
		return true;
	}
}
