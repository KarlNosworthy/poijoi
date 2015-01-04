package com.karlnosworthy.poijoi.io.reader.ods;

import java.io.InputStream;

import org.odftoolkit.simple.SpreadsheetDocument;

import com.karlnosworthy.poijoi.io.reader.InputStreamReader;

/**
 * An ODSInputStreamReader interacts with the ODS file using an {@link InputStream}
 * 
 * @author john.bartlett
 *
 */
public class ODSInputStreamReader extends AbstractODSReader<InputStream> implements
		InputStreamReader {

	@Override
	SpreadsheetDocument getDocument(InputStream source) throws Exception {
		return SpreadsheetDocument.loadDocument(source);
	}

}
