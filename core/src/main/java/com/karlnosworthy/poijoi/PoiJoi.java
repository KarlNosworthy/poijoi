package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.karlnosworthy.poijoi.core.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.io.PoiJoiManager;
import com.karlnosworthy.poijoi.io.Reader;
import com.karlnosworthy.poijoi.io.Writer;
import com.karlnosworthy.poijoi.io.Writer.WriteType;

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
	private String outputQualifier;
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
	 */
	public String getOutputQualifier() {
		return this.outputQualifier;
	}

	/**
	 */
	public void setOutputQualifier(String outputQualifier) throws IOException {
		if (outputQualifier == null) {
			throw new IllegalArgumentException(
					"The output path file instance cannot be null.");
/*			
		} else if (outputPath.isFile() && outputPath.exists()) {
			throw new IOException(
					"The output path specified a file that already exists.");
*/					
		}

		this.outputQualifier = outputQualifier;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public void process() throws Exception {
		PoiJoiManager poiJoiManager = new PoiJoiManager();
		
		Reader reader = poiJoiManager.findReader(sourceDataFile.getAbsolutePath());
		Writer writer = poiJoiManager.findWriter(outputQualifier);
		
		PoijoiMetaData metaData = reader.read(sourceDataFile.getAbsolutePath(), true);
		writer.write(outputQualifier, metaData, WriteType.BOTH);
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
					poiJoiInstance.setOutputQualifier(args[2]);
				} else if (args.length == 2) {
					poiJoiInstance.setSourceDataFile(new File(args[0]));
					poiJoiInstance.setOutputQualifier(args[1]);
				} else {
					poiJoiInstance.setOptions(parseOptions(args[0]));
					File sourcePath = poiJoiInstance.getSourceDataFile();
					if (sourcePath.isDirectory()) {
						poiJoiInstance.setOutputQualifier(sourcePath
								.getAbsolutePath());
					} else {
						int index = sourcePath.getAbsolutePath().lastIndexOf(
								File.separator);
						poiJoiInstance.setOutputQualifier(sourcePath
								.getAbsolutePath().substring(0, index));
					}
				}

				poiJoiInstance.process();
			} catch (Exception ioe) {
				System.out.println("ERROR: " + ioe.getLocalizedMessage());
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
