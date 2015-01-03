package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;
import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;

/**
 * The main PoiJoi class which can be used inside a framework or as a
 * stand-alone command line application to create and populate a database from a
 * source data file.
 * 
 * @author Karl Nosworthy
 * @since 1.0
 */
public class PoiJoi {

	private static final Logger logger = LoggerFactory.getLogger(PoiJoi.class);
	private String inputQualifier;
	private String outputQualifier;
	private Map<String, String> options;

	/**
	 * Constructs a new PoiJoi instance.
	 */
	public PoiJoi() {
		super();
		this.options = new HashMap<String, String>();
	}

	public String getSourceDataFile() {
		return inputQualifier;
	}

	public void setInputQualifier(String inputQualifier) throws IOException {
		if (inputQualifier == null) {
			throw new IllegalArgumentException(
					"The input qualifier cannot be null.");
		}

		this.inputQualifier = inputQualifier;
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
			 * } else if (outputPath.isFile() && outputPath.exists()) { throw
			 * new IOException(
			 * "The output path specified a file that already exists.");
			 */
		}

		this.outputQualifier = outputQualifier;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public void process() throws Exception {
		PoiJoiManager poiJoiManager = new PoiJoiManager(new PoiJoiOptions(options));

		FormatType inputFormat = determineFormatType(inputQualifier);

		Object inputSource = null;
		if (inputFormat == FormatType.SQLITE) {
			inputSource = DriverManager.getConnection(inputQualifier);
		} else {
			inputSource = new File(inputQualifier);
		}
		
		logger.info("Input Format: {}, Input Source: {} ", inputFormat, inputSource);

		FormatType outputFormat = determineFormatType(outputQualifier);
		
		Object output = null;
		if (outputFormat == FormatType.SQLITE) {
			output = DriverManager.getConnection(outputQualifier);
		} else {
			output = new File(outputQualifier);
		}

		logger.info("Output Format: {}, output:  {}", outputFormat, output);
		
		Reader reader = poiJoiManager.findReader(inputSource, inputFormat);
		
		if (reader == null) {
			logger.info("No Reader Found for Format {} and input source {}", inputFormat, inputSource);
		}
		
		Writer writer = poiJoiManager.findWriter(output, outputFormat);
		
		if (writer == null) {
			logger.info("No Writer Found for Format {} and input source {}", outputFormat, output);
		}

		PoijoiMetaData metaData = reader.read(inputSource, true);
		writer.write(output, metaData, WriteType.BOTH);
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
			logger.info(PoiJoi.getUsageString());
		} else {
			PoiJoi poiJoiInstance = new PoiJoi();
			try {
				if (args.length > 2) {
					poiJoiInstance.setOptions(parseOptions(args[0]));
					poiJoiInstance.setInputQualifier(args[1]);
					poiJoiInstance.setOutputQualifier(args[2]);
				} else if (args.length == 2) {
					poiJoiInstance.setInputQualifier(args[0]);
					poiJoiInstance.setOutputQualifier(args[1]);
				} else {
					poiJoiInstance.setOptions(parseOptions(args[0]));
					/*
					 * poiJoiInstance.setInputQualifier(inputQualifier); File
					 * sourcePath = poiJoiInstance.getSourceDataFile(); if
					 * (sourcePath.isDirectory()) {
					 * poiJoiInstance.setOutputQualifier(sourcePath
					 * .getAbsolutePath()); } else { int index =
					 * sourcePath.getAbsolutePath().lastIndexOf(
					 * File.separator);
					 * poiJoiInstance.setOutputQualifier(sourcePath
					 * .getAbsolutePath().substring(0, index)); }
					 */
				}

				poiJoiInstance.process();
			} catch (Exception ioe) {
				logger.error(ioe.getMessage(), ioe);
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

	private FormatType determineFormatType(String qualifier) {
		FormatType formatType = null;

		if (isFile(qualifier)) {
			File qualifierFile = new File(qualifier);

			String qualifierFilePath = qualifierFile.getAbsolutePath();

			if (qualifierFilePath.endsWith(FormatType.XLS.name().toLowerCase())) {
				formatType = FormatType.XLS;
			} else if (qualifierFilePath.endsWith(FormatType.XLSX.name()
					.toLowerCase())) {
				formatType = FormatType.XLSX;
			} else if (qualifierFilePath.endsWith(FormatType.ODS.name()
					.toLowerCase())) {
				formatType = FormatType.ODS;
			}
		} else if (isJdbcURL(qualifier)) {
			int procotolEndIndex = (1 + qualifier.indexOf(":"));
			int subProtocolEndIndex = qualifier.indexOf(":", procotolEndIndex);

			String subProtocol = qualifier.substring(procotolEndIndex,
					subProtocolEndIndex);

			if (subProtocol.equalsIgnoreCase(FormatType.SQLITE.name())) {
				formatType = FormatType.SQLITE;
			}
		}

		return formatType;
	}

	private boolean isFile(String inputSource) {
		if (inputSource == null || inputSource.length() == 0) {
			return false;
		}

		if (inputSource.startsWith(File.separator)) {
			return true;
		}

		return false;
	}

	private boolean isJdbcURL(String output) {
		if (output == null || output.length() == 0) {
			return false;
		}
		if (output.startsWith("jdbc:")) {
			return true;
		}
		return false;
	}
}
