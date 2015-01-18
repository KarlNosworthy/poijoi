package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PoiJoiLauncher implements PoiJoiRegistrationListener {

	private static final Logger logger = LoggerFactory.getLogger(PoiJoiLauncher.class);
	private String inputQualifier;
	private String outputQualifier;
	private Map<String, String> options;
	private int numberOfExtensionsAvailable;
	
	
	/**
	 * Constructs a new PoiJoi instance.
	 */
	public PoiJoiLauncher() {
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
		}
		this.outputQualifier = outputQualifier;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public void process() throws Exception {
		
		PoiJoi poiJoi = null;

		PoiJoiOptions poiJoiOptions = new PoiJoiOptions(options);
		
		if (poiJoiOptions.hasValue(PoiJoiOptions.OPTION_INFO)) {
			poiJoi = new PoiJoi(poiJoiOptions, this);
		} else {
			poiJoi = new PoiJoi(poiJoiOptions);
		
			String inputFormat = determineFormatType(inputQualifier);
	
			Object input = null;
			
			if (isJdbcURL(inputQualifier)) {
				String connectionURL = makeAnyFilePathAbsolute(inputQualifier);
				input = DriverManager.getConnection(connectionURL);
			} else {
				input = new File(inputQualifier);
			}
			
			logger.info("Input Format: {}, Input Source: {} ", inputFormat, input);
	
			String outputFormat = determineFormatType(outputQualifier);
			
			Object output = null;
			if (isJdbcURL(outputQualifier)) {
				String connectionURL = makeAnyFilePathAbsolute(outputQualifier);
				output = DriverManager.getConnection(connectionURL);
			} else {
				output = new File(outputQualifier);
			}
			
			PoijoiMetaData metadata = poiJoi.read(input, inputFormat, true);
			poiJoi.write(metadata, output, outputFormat, WriteType.BOTH);
		}
	}

	@Override
	public void registrationStarted() {
		System.out.println("");
	}

	@Override
	public void registeredExtension(String className, String packageName, String formatType,
			ExtensionType extensionType) {
		System.out.println(" > "+className+" [FOUND - "+extensionType.name()+"]");
		numberOfExtensionsAvailable += 1;
	}
	
	@Override
	public void registrationFinished() {
		System.out.println("\n" + numberOfExtensionsAvailable+" extension(s) available.\n");
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
			System.out.println(PoiJoiLauncher.getUsageString());
		} else {
			System.out.println("\nPoiJoiLauncher");
			PoiJoiLauncher poiJoiInstance = new PoiJoiLauncher();
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
				}
				poiJoiInstance.process();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static String getUsageString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PoiJoiLauncher [options] <path to datafile> [outputpath]");
		return builder.toString();
	}

	private static Map<String, String> parseOptions(String optionsArgsString) {
		Map<String, String> parsedOptions = new HashMap<String, String>();

		String[] optionsItems = optionsArgsString.split("=");
		
		if (optionsItems.length > 1) {
			for (int optionItemIndex = 0; optionItemIndex < optionsItems.length; optionItemIndex += 2) {
				parsedOptions.put(optionsItems[optionItemIndex],
						optionsItems[1 + optionItemIndex]);
			}
		} else {
			parsedOptions.put(optionsItems[0], "");
		}
		
		return parsedOptions;
	}

	private String determineFormatType(String qualifier) {
		String formatType = null;

		if (isFile(qualifier)) {
			File qualifierFile = new File(qualifier);
			String qualifierFilePath = qualifierFile.getAbsolutePath();
			int fileExtensionIndex = qualifierFilePath.lastIndexOf(".");
			formatType = qualifierFilePath.substring(1 + fileExtensionIndex);
		} else if (isJdbcURL(qualifier)) {
			int procotolEndIndex = 1 + qualifier.indexOf(":");
			int subProtocolEndIndex = qualifier.indexOf(":", procotolEndIndex);

			String subProtocol = qualifier.substring(procotolEndIndex,
					subProtocolEndIndex);

			formatType = subProtocol;
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
	
	private String makeAnyFilePathAbsolute(String jdbcUrl) {
		int pathSeparator = jdbcUrl.lastIndexOf(":");
		String path = jdbcUrl.substring(1 + pathSeparator);
		
		if (path.startsWith("~")) {
			path = path.replace("~", System.getProperty("user.home"));
		}
		
		File filePath = new File(path);
		return jdbcUrl.substring(0,1 + pathSeparator).concat(filePath.getAbsolutePath());
	}
}
