package com.karlnosworthy.poijoi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.OptionAware;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.reflect.PoiJoiClassTools;
import com.karlnosworthy.poijoi.io.reflect.PoiJoiClasspathScanner;
import com.karlnosworthy.poijoi.io.writer.Writer;
import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;

/**
 * The central access to PoiJoi functionality where readers and writers are loaded and can be obtained.
 * 
 * An instance can be configuring using a {@link PoiJoiOptions} instance or left as defaults. If provided, these options
 * are then passed onto any Reader or Writer instance that implements the {@link OptionAware} interface.
 * 
 * WARNING: A cache of applicable reader and writer classes is created on construction by searching the classpath
 *          for applicable implementations.  It's suggested that unless otherwise required, a single instance is
 *          used.
 * 
 * @author Karl Nosworthy
 * @version 1.0
 */
public class PoiJoi {
	
	private static final Logger logger = LoggerFactory.getLogger(PoiJoi.class);
	
	private final String CUSTOM_PACKAGE_NAME_OPTION = "package";
	
	private PoiJoiOptions options;
	private PoiJoiClasspathScanner classpathScanner;
	private PoiJoiClassTools classTools;
	
	/**
	 * Creates an instance of PoiJoi which is configured using the standard options.
	 */
	public PoiJoi() {
		this(null, null);
	}
	
	/**
	 * Creates an instance of PoiJoi which is configured using the specified options.
	 * 
	 * @param options The options to use when configuring
	 */
	public PoiJoi(PoiJoiOptions options) {
		this(options, null);
	}
	
	public PoiJoi(PoiJoiOptions options, PoiJoiRegistrationListener registrationListener) {
		super();
		this.options = options;
		this.classTools = new PoiJoiClassTools();
		this.classpathScanner = new PoiJoiClasspathScanner(classTools);
		this.classpathScanner.addRootPackageName(Reader.class.getPackage().getName());
		this.classpathScanner.addRootPackageName(Writer.class.getPackage().getName());
		
		if (options != null && options.hasValue(CUSTOM_PACKAGE_NAME_OPTION)) {
			this.classpathScanner.addRootPackageName(options.getValue(CUSTOM_PACKAGE_NAME_OPTION));
		}
		
		this.classpathScanner.scan(registrationListener);
	}
	
	public <T> PoijoiMetaData read(T input, String formatType, boolean readData) throws Exception {
		Reader<T> reader = findReader(input, formatType);
		if (reader != null) {
			return reader.read(input, readData);
		}
		return null;
	}
	
	public <T> boolean write(PoijoiMetaData metaData, T output, String formatType, WriteType writeType) throws Exception {
		Writer<T> writer = findWriter(output, formatType);
		if (writer != null) {
			return writer.write(output, metaData, writeType);
		}
		return false;
	}
	
	/**
	 * Searches for a known reader that supports the given input type and format.
	 * 
	 * @param input The type of input that the reader needs to support.
	 * @param formatType The format type of the data that the reader will be reading.
	 * @return An instance of an applicable reader (may be null).
	 */
	public <T> Reader<T> findReader(T input, String formatType) {
		Reader<T> reader = getCachedReader(input, formatType);
		if (reader instanceof OptionAware) {
			((OptionAware) reader).setOptions(options);
		}
		return reader;
	}
	
	/**
	 * Searches for a known writer that supports the given input type and format.
	 * 
	 * @param output The type of output that the reader needs to support.
	 * @param formatType The format type of the data that the reader will be reading.
	 * @return An instance of an applicable reader (may be null).
	 */
	public <T> Writer<T> findWriter(T output, String formatType) {
		Writer<T> writer = getCachedWriter(output, formatType);
		if (writer instanceof OptionAware) {
			((OptionAware) writer).setOptions(options);
		}
		return writer;
	}	
	
	/**
	 * Attempts to find and return an instance of a reader from the cache. If an instance
	 * cannot be found but a supporting class is available, a new instance will be created, 
	 * cached and then returned.
	 * 
	 * @param input The type of input that the reader needs to support.
	 * @param formatType The format type of the data that the reader will be reading.
	 * @return An instance of an applicable reader or null.
	 */
	private <T> Reader<T> getCachedReader(T input, String formatType) {
		Reader<T> reader = null;
		
		System.out.println("CachedReader: "+input.getClass()+" / "+formatType);
		Class<?> readerClass = classpathScanner.getCachedReaderClass(input.getClass(), formatType);
		
		if (readerClass != null) {
			try {
				reader = classTools.createReaderInstance(input, readerClass);
			} catch (IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
		return reader;
	}
	
	/**
	 * Attempts to find and return an instance of a writer from the cache. If an instance
	 * cannot be found but a supporting class is available, a new instance will be created, 
	 * cached and then returned.
	 * 
	 * @param output The type of output that the writer needs to support.
	 * @param formatType The format type of the data that the writer will be reading.
	 * @return An instance of an applicable writer or null.
	 */
	private <T> Writer<T> getCachedWriter(T output, String formatType) {
		Writer<T> writer = null;
		
		Class<?> writerClass = classpathScanner.getCachedWriterClass(output.getClass(), formatType);
		
		if (writerClass != null) {
			try {
				writer = classTools.createWriterInstance(output, writerClass);
			} catch (IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
		return writer;
	}
}
