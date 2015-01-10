package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.io.OptionAware;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.FileReader;
import com.karlnosworthy.poijoi.io.reader.JDBCConnectionReader;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.FileWriter;
import com.karlnosworthy.poijoi.io.writer.JDBCConnectionWriter;
import com.karlnosworthy.poijoi.io.writer.Writer;

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
	
	private final String CLASS_EXTENSION = ".class";
	private final String JAR_PROTOCOL_NAME = "jar";
	private final String TEST_CLASS_NAME_SUFFIX = "Test.class";
	
	private final String CUSTOM_PACKAGE_NAME_OPTION = "package";
	
	private Set<Class<?>> readerClassCache;
	private HashMap<String, Reader<?>> readerInstanceCache;
	private Set<Class<?>> writerClassCache;
	private HashMap<String, Writer<?>> writerInstanceCache;
	private String rootReaderPackageName;
	private String rootWriterPackageName;
	private PoiJoiOptions options;
	
	
	/**
	 * Creates an instance of PoiJoi which is configured using the standard options.
	 */
	public PoiJoi() {
		this(null);
	}
	
	/**
	 * Creates an instance of PoiJoi which is configured using the specified options.
	 * 
	 * @param options The options to use when configuring
	 */
	public PoiJoi(PoiJoiOptions options) {
		super();
		this.options = options;
		this.rootReaderPackageName = Reader.class.getPackage().getName();
		this.rootWriterPackageName = Writer.class.getPackage().getName();
		this.readerClassCache = new HashSet<Class<?>>();
		this.writerClassCache = new HashSet<Class<?>>();
		findAndCacheReadersAndWriters();
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
	@SuppressWarnings("unchecked")
	private <T> Reader<T> getCachedReader(T input, String formatType) {
		Reader<T> reader = null;
		
		if (readerInstanceCache != null && !readerInstanceCache.isEmpty()) {
			if (readerInstanceCache.containsKey(formatType + input.getClass().getName())) {
				reader = (Reader<T>) readerInstanceCache.get(formatType);
			}
		}
		
		if (reader == null) {
			if (readerClassCache != null && !readerClassCache.isEmpty()) {
				
				Iterator<Class<?>> readerClassIterator = readerClassCache.iterator();
				while (readerClassIterator.hasNext()) {
					Class<?> readerClass =  (Class<?>) readerClassIterator.next();
					
					if (supportsFormat(readerClass, formatType)) {
						try {
							reader = (Reader<T>) readerClass.newInstance();
							if (readerInstanceCache == null) {
								readerInstanceCache = new HashMap<String,Reader<?>>();
							}
							readerInstanceCache.put(formatType + input.getClass().getName(), reader);
						} catch (InstantiationException instantiationException) {
							instantiationException.printStackTrace();
						} catch (IllegalAccessException illegalAccessException) {
							illegalAccessException.printStackTrace();
						}
					}
				}
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
	@SuppressWarnings("unchecked")
	private <T> Writer<T> getCachedWriter(T output, String formatType) {
		Writer<T> writer = null;
		
		if (writerInstanceCache != null && !writerInstanceCache.isEmpty()) {
			if (writerInstanceCache.containsKey(formatType)) {
				writer = (Writer<T>) writerInstanceCache.get(formatType + output.getClass().getName());
			}
		}
		
		if (writer == null) {
			if (writerClassCache != null && !writerClassCache.isEmpty()) {
				
				Iterator<Class<?>> writerClassIterator = writerClassCache.iterator();
				
				while (writerClassIterator.hasNext()) {
					Class<?> writerClass =  (Class<?>) writerClassIterator.next();
					
					if (supportsFormat(writerClass, formatType)) {
						try {
							writer = (Writer<T>) writerClass.newInstance();
							if (writerInstanceCache == null) {
								writerInstanceCache = new HashMap<String,Writer<?>>();
							}
							writerInstanceCache.put(formatType + output.getClass().getName(), writer);
						} catch (InstantiationException instantiationException) {
							instantiationException.printStackTrace();
						} catch (IllegalAccessException illegalAccessException) {
							illegalAccessException.printStackTrace();
						}
					}
				}
			}		
		}
		return writer;
	}
	
	/**
	 * Used to determine if the specified class can support I/O with the
	 * format type provided.
	 * 
	 * @param _class The class to be checked
	 * @param formatType The format type to check against.
	 * @return True if the class contains a 'SupportsFormat' annotation which matches the format type otherwise false.
	 */
	private boolean supportsFormat(Class<?> _class, String formatType) {
		Annotation annotation = (Annotation) _class.getAnnotation(SupportsFormat.class);
		if (annotation instanceof SupportsFormat) {
			SupportsFormat supportsFormatAnnotation = (SupportsFormat) annotation;
			if (supportsFormatAnnotation.type().equalsIgnoreCase(formatType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches the classpath for classes implementing the Reader or Writer interfaces which 
	 * also contain the 'SupportsFormat' annotation.
	 * 
	 * @throws IOException
	 */
	private void findAndCacheReadersAndWriters() {
		
		List<String> rootPackageNamesList = new ArrayList<String>();
		rootPackageNamesList.add(rootReaderPackageName);
		rootPackageNamesList.add(rootWriterPackageName);
		
		if (options != null && options.hasValue(CUSTOM_PACKAGE_NAME_OPTION)) {
			rootPackageNamesList.add(options.getValue(CUSTOM_PACKAGE_NAME_OPTION));
		}
		
		for (String rootPackageName: rootPackageNamesList) {
			String resourcePath = createResourceName(rootPackageName);
			
			logger.info("Checking for classes on resource path {} ", resourcePath);
			
			Set<Class<?>> classes = new HashSet<Class<?>>();
			
			Enumeration<URL> resourceURLs;
			try {
				resourceURLs = getClass().getClassLoader().getResources(resourcePath);
				while (resourceURLs.hasMoreElements()) {
					URL resourceURL = resourceURLs.nextElement();
					
					if (resourceURL.getProtocol().equals(JAR_PROTOCOL_NAME)) {
						URLConnection con = resourceURL.openConnection();
						JarFile jarFile = null;
	
						if (con instanceof JarURLConnection) {
							JarURLConnection jarCon = (JarURLConnection) con;
							jarCon.setUseCaches(false);
							jarFile = jarCon.getJarFile();
							
							classes.addAll(findClassesInEntries(jarFile, rootPackageName));
						}
					} else {
						File[] subDirectories = obtainSubDirectoryNames(new File(resourceURL.getFile()));
						
						for (File subDirectory : subDirectories) {
							File[] potentialClassFilenames = obtainClassFilenames(subDirectory);
							
							String packageName = makePackageName(rootPackageName, subDirectory.getName(), true);
							for (File potentialClassFilename : potentialClassFilenames) {
								try {
									classes.add(Class.forName(packageName + potentialClassFilename.getName().replace(CLASS_EXTENSION, "")));
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}					
					}
					
					for (Class<?> _class : classes) {
						if (isValidReaderImplementation(_class)) {
							logger.info("Found Reader implementation {} ", _class.getCanonicalName());
							readerClassCache.add(_class);
						} else if (isValidWriterImplementation(_class)) {
							logger.info("Found Writer implementation {} ", _class.getCanonicalName());
							writerClassCache.add(_class);
						}
					}				
				}
			} catch (IOException ioException) {
				logger.debug("", ioException);
			}
		}
	}
	
	/**
	 * Searches for classes inside a jar file.
	 * 
	 * @param rootPackageName The root package to check under.
	 * @param jarFile The Jar file to check.
	 * @return A set containing any classes found.
	 */
	private Set<Class<?>> findClassesInEntries(JarFile jarFile, String rootPackageName) {
		Set<Class<?>> classes = new HashSet<Class<?>>();

		String resourceName = createResourceName(rootPackageName);
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			String entryPath = entry.getName();

			if (entryPath.startsWith(resourceName) && entryPath.endsWith(CLASS_EXTENSION)) {
				String className = entryPath.replace('/', '.').replace(CLASS_EXTENSION, "");
				try {
					Class<?> _clazz = getClass().getClassLoader().loadClass(className);
					classes.add(_clazz);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return classes;
	}
	
	/**
	 * Used to determine if the specified class is recognised as a valid Reader implementation.
	 * 
	 * @param The class to check
	 * @return True if class implements one of the know Reader interfaces and its not an interface itself otherwise false.
	 */
	private boolean isValidReaderImplementation(Class<?> _class) {
		if ((Reader.class.isAssignableFrom(_class) && !_class.isInterface()) ||
		    (FileReader.class.isAssignableFrom(_class)  && !_class.isInterface()) ||
		    (JDBCConnectionReader.class.isAssignableFrom(_class) && !_class.isInterface())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Used to determine if the specified class is recognised as a valid Writer implementation.
	 * 
	 * @param The class to check
	 * @return True if class implements one of the know Writer interfaces and its not an interface itself otherwise false.
	 */
	private boolean isValidWriterImplementation(Class<?> _class) {
		if ((Writer.class.isAssignableFrom(_class) && !_class.isInterface()) ||
			(FileWriter.class.isAssignableFrom(_class) && !_class.isInterface()) ||
			(JDBCConnectionWriter.class.isAssignableFrom(_class) && !_class.isInterface())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Changes the given package name into a resource name element.
	 * 
	 * @param packageName The package name to convert.
	 * @return The resource name element for the given package name.
	 */
	private String createResourceName(String packageName) {
		return (packageName.replace('.', '/') + '/');
	}

	/**
	 * Creates a package name from the root and sub package names supplied.
	 * 
	 * @param rootPackageName The root package name to be used.
	 * @param subPackageName The sub package name to be used.
	 * @param appendFinalSeparator Flag to determine if a separator should be appended to the final string.
	 * @return A string containing the correctly formatted package name.
	 */
	private String makePackageName(String rootPackageName, String subPackageName, boolean appendFinalSeparator) {
		StringBuilder builder = new StringBuilder();
		builder.append(rootPackageName);
		builder.append(".");
		builder.append(subPackageName);

		if (appendFinalSeparator) {
			builder.append(".");
		}
		return builder.toString();
	}
	
	/**
	 * Provides a list of sub directory names which are contained within the given root directory.
	 * 
	 * @param rootDir The root directory to check.
	 * @return An array of the files which contain the root directories sub directories.
	 */
	private File[] obtainSubDirectoryNames(File rootDir) {
		return rootDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.indexOf(".") < 0) {
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * Provides a list of the class filenames which are contained within the given root directory.
	 * 
	 * @param rootDir The root directory to check.
	 * @return An array which contain the root directories class filenames.
	 */
	private File[] obtainClassFilenames(File rootDir) {
		return rootDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(CLASS_EXTENSION) && name.indexOf("$") < 0 &&
					!name.equals(Reader.class.getName()) &&
					!name.equals(Writer.class.getName()) &&
					!name.endsWith(TEST_CLASS_NAME_SUFFIX)) {
					return true;
				}
				return false;
			}
		});
	}
}
