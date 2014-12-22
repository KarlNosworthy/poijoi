package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.Reader;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;
import com.karlnosworthy.poijoi.io.ods.ODSSpreadsheetReader;
import com.karlnosworthy.poijoi.io.sqlite.SQLiteDatabaseCreator;
import com.karlnosworthy.poijoi.io.xls.XLSSpreadsheetReader;

/**
 * The main PoiJoi class which can be used inside a framework or as a
 * stand-alone command line application to create and populate a database from a
 * source data file.
 * 
 * @author Karl Nosworthy
 * @since 1.0
 */
public class PoiJoi {

	private File sourceDataFile;
	private File outputPath;
	private Map<String, String> options;

	/**
	 * Constructs a new PoiJoi instance.
	 */
	public PoiJoi() {
		super();
		this.options = new HashMap<String, String>();
	}

	/**
	 * Returns the File instance that points towards the data file which is to
	 * be used to specify and populate the database.
	 * 
	 * @return The File instance.
	 */
	public File getSourceDataFile() {
		return this.sourceDataFile;
	}

	/**
	 * Sets the File instance that points to the data file which is to be used
	 * to specify and populate the database. The File instance must point
	 * towards a file that exists and is readable by the current process.
	 * 
	 * @param sourceDataFile
	 *            The File instance to use.
	 * @throws IllegalArgumentException
	 *             Thrown if the parameter provided is null.
	 * @throws IOException
	 *             Thrown if the file does not exist or cannot be read from the
	 *             file system.
	 */
	public void setSourceDataFile(File sourceDataFile) throws IOException {
		if (sourceDataFile == null) {
			throw new IllegalArgumentException(
					"The source data file instance cannot be null.");
		} else if (!sourceDataFile.exists()) {
			throw new IOException(
					"The source data file specified does not exist in the file system.");
		} else if (!sourceDataFile.canRead()) {
			throw new IOException(
					"The source data file specified is not readable.");
		}

		this.sourceDataFile = sourceDataFile;
	}

	/**
	 * Returns the File instance containing the details of where to write the
	 * populated database.
	 * 
	 * @return The File instance.
	 */
	public File getOutputPath() {
		return this.outputPath;
	}

	/**
	 * Sets the path to be used when writing the database output file. The File
	 * instance can point towards a directory that already exists or specify a
	 * filename that doesn't.
	 * 
	 * @param outputPath
	 *            The File containing the directory and/or filename to use.
	 * @throws IllegalArgumentException
	 *             Thrown if the parameter provided is null.
	 * @throws IOException
	 *             Thrown if the output path points to a file that already
	 *             exists.
	 */
	public void setOutputPath(File outputPath) throws IOException {
		if (outputPath == null) {
			throw new IllegalArgumentException(
					"The output path file instance cannot be null.");
		} else if (outputPath.isFile() && outputPath.exists()) {
			throw new IOException(
					"The output path specified a file that already exists.");
		}

		this.outputPath = outputPath;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public void process() throws Exception {
		// Read
		Reader reader = null;
		String inputFilePath = sourceDataFile.getAbsolutePath();
		if (inputFilePath
				.endsWith(PoiJoiConfiguration.OPEN_OFFICE_DOCUMENT_EXTENSION)) {
			reader = new ODSSpreadsheetReader();
		} else if (inputFilePath
				.endsWith(PoiJoiConfiguration.MS_EXCEL_DOCUMENT_EXTENSION)) {
			reader = new XLSSpreadsheetReader();
		}
		PoijoiMetaData metaData = reader.read(inputFilePath, true);
		
		// Write
		Writer writer = null;
		// instantiate JDBC creator for the second
		writer = new SQLiteDatabaseCreator();
		writer.write(outputPath.getAbsolutePath(), metaData, WriteType.BOTH);
		
	}

	/**
	 * The command line entry point which supports the following usage:
	 * 
	 * Usage: PoiJoi <path to data file> [output path]
	 * 
	 * @param args
	 *            The command line arguments provided.
	 */
	public static void main(String... args) {

		if (args.length == 0) {
			System.out.println(PoiJoi.getUsageString());
		} else {
			PoiJoi poiJoiInstance = new PoiJoi();
			try {
				if (args.length > 2) {
					poiJoiInstance.setOptions(parseOptions(args[0]));
					poiJoiInstance.setSourceDataFile(new File(args[1]));
					poiJoiInstance.setOutputPath(new File(args[2]));
				} else if (args.length == 2) {
					poiJoiInstance.setSourceDataFile(new File(args[0]));
					poiJoiInstance.setOutputPath(new File(args[1]));
				} else {
					poiJoiInstance.setOptions(parseOptions(args[0]));
					File sourcePath = poiJoiInstance.getSourceDataFile();
					if (sourcePath.isDirectory()) {
						poiJoiInstance.setOutputPath(new File(sourcePath
								.getAbsolutePath()));
					} else {
						int index = sourcePath.getAbsolutePath().lastIndexOf(
								File.separator);
						poiJoiInstance.setOutputPath(new File(sourcePath
								.getAbsolutePath().substring(0, index)));
					}
				}

				poiJoiInstance.process();
			} catch (Exception ioe) {
				System.out.println("ERROR: " + ioe.getMessage());
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static String getUsageString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PoiJoi [options] <path to datafile> [outputpath]");
		return builder.toString();
	}

	private static Map<String, String> parseOptions(String optionsArgsString) {
		Map<String, String> parsedOptions = new HashMap<String, String>();

		String[] optionsItems = optionsArgsString.split("=");
		for (int optionItemIndex = 0; optionItemIndex < optionsItems.length; optionItemIndex += 2) {
			parsedOptions.put(optionsItems[optionItemIndex],
					optionsItems[1 + optionItemIndex]);
		}
		return parsedOptions;
	}
}
