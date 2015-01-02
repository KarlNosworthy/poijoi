package com.karlnosworthy.poijoi.io.writer.ods;

import java.io.File;

import org.odftoolkit.simple.SpreadsheetDocument;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.writer.FileWriter;

@SupportsFormat(type = FormatType.ODS)
public class ODSFileWriter extends AbstractODSWriter<File> implements FileWriter {

	@Override
	void write(File output, SpreadsheetDocument spreadsheetDocument) throws Exception {
		spreadsheetDocument.save(output);
	}

}
