package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.PoiJoiMetaData;

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
	private String primaryQualifier;
	private String secondaryQualifier;
	private Map<String, String> options;
	private int numberOfExtensionsAvailable;
	
	
	/**
	 * Constructs a new PoiJoi instance.
	 */
	public PoiJoiLauncher() {
		super();
		this.options = new HashMap<String, String>();
	}

	public String getPrimaryQualifier() {
		return primaryQualifier;
	}

	public void setPrimaryQualifier(String primaryQualifier) throws IOException {
		if (primaryQualifier == null) {
			throw new IllegalArgumentException(
					"The primary/input qualifier cannot be null.");
		}

		this.primaryQualifier = primaryQualifier;
	}

	/**
	 */
	public String getSecondaryQualifier() {
		return this.secondaryQualifier;
	}

	/**
	 */
	public void setSecondaryQualifier(String secondaryQualifier) throws IOException {
		if (secondaryQualifier == null) {
			throw new IllegalArgumentException(
					"The secondary/output path file instance cannot be null.");
		}
		this.secondaryQualifier = secondaryQualifier;
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
			
			String primaryFormat = determineFormatType(primaryQualifier);
			String secondaryFormat = determineFormatType(secondaryQualifier);
			
			Object primaryQualifierHandle = null;
			Object secondaryQualifierHandle = null;
			
			
			if (isJdbcURL(primaryQualifier)) {
				String connectionURL = makeAnyFilePathAbsolute(primaryQualifier);
				primaryQualifierHandle = DriverManager.getConnection(connectionURL);
			} else {
				primaryQualifierHandle = new File(primaryQualifier);
			}
			
			logger.info("Input Format: {}, Input Source: {} ", primaryFormat, primaryQualifierHandle);
			
			if (isJdbcURL(secondaryQualifier)) {
				String connectionURL = makeAnyFilePathAbsolute(secondaryQualifier);
				secondaryQualifierHandle = DriverManager.getConnection(connectionURL);
			} else {
				secondaryQualifierHandle = new File(secondaryQualifier);
			}
			
			logger.info("Output Format: {}, Output Source: {} ", secondaryFormat, secondaryQualifierHandle);			
			
			if (poiJoiOptions.hasValue("--compare")) {
				PoiJoiMetaData primaryMetaData = poiJoi.read(primaryQualifierHandle, primaryFormat, true);
				PoiJoiMetaData secondaryMetaData = poiJoi.read(secondaryQualifierHandle, secondaryQualifier, true);

				if (primaryMetaData.isSameAs(secondaryMetaData)) {
					logger.info("Match");
				}
			} else { // Normal I/O
				PoiJoiMetaData metadata = poiJoi.read(primaryQualifierHandle, primaryFormat, true);
				logger.info("Metadata: {} ",metadata);
				poiJoi.write(metadata, secondaryQualifierHandle, secondaryFormat, WriteType.BOTH);
			}
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
					poiJoiInstance.setPrimaryQualifier(args[1]);
					poiJoiInstance.setSecondaryQualifier(args[2]);
				} else if (args.length == 2) {
					poiJoiInstance.setPrimaryQualifier(args[0]);
					poiJoiInstance.setSecondaryQualifier(args[1]);
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
