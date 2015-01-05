package com.karlnosworthy.poijoi.io.reader.ods;

import java.io.File;

import org.odftoolkit.simple.SpreadsheetDocument;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.FileReader;

/**
 * An ODSFileReader interacts with an ODS file using a {@link File}
 * 
 * @author john.bartlett
 *
 */
@SupportsFormat(type = FormatType.ODS)
public class ODSFileReader extends AbstractODSReader<File> implements
		FileReader {

	@Override
	SpreadsheetDocument getDocument(File input) throws Exception {
		return SpreadsheetDocument.loadDocument(input);
	}

	@Override
	boolean isValidInput(File input) {
		if (input == null || input.isDirectory()) {
			return false;
		}
		return true;
	}
}
