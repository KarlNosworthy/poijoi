package com.karlnosworthy.poijoi.io.reflect;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlnosworthy.poijoi.PoiJoiRegistrationListener;
import com.karlnosworthy.poijoi.PoiJoiRegistrationListener.ExtensionType;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;

public class PoiJoiClasspathScanner {
	
	private static final Logger logger = LoggerFactory.getLogger(PoiJoiClasspathScanner.class);

	private final String CLASS_EXTENSION = ".class";
	private final String JAR_PROTOCOL_NAME = "jar";
	private final String TEST_CLASS_NAME_SUFFIX = "Test.class";
	
	private List<String> rootPackageNames;
	private PoiJoiClassTools classTools;
	private Set<CacheEntry> readerClassCache;
	private Set<CacheEntry> writerClassCache;
	
	public PoiJoiClasspathScanner() {
		this(new PoiJoiClassTools());
	}
	
	public PoiJoiClasspathScanner(PoiJoiClassTools classTools) {
		super();
		this.classTools = classTools;
		this.rootPackageNames = new ArrayList<String>();
		this.readerClassCache = new HashSet<CacheEntry>();
		this.writerClassCache = new HashSet<CacheEntry>();
	}
	
	public void addRootPackageName(String rootPackageName) {
		this.rootPackageNames.add(rootPackageName);
	}
	
	public List<String> getRootPackageNames() {
		return rootPackageNames;
	}
	
	public boolean hasReaderCache() {
		return readerClassCache != null && !readerClassCache.isEmpty();
	}
	
	public boolean hasWriterCache() {
		return writerClassCache != null && !writerClassCache.isEmpty();
	}
	
	public <T> Class<?> getCachedReaderClass(Type inputResourceType, String formatType) {
		Class<?> cachedReaderClass = null;
		
		for (CacheEntry cacheEntry : readerClassCache) {
			if (cacheEntry.getFormatType().equalsIgnoreCase(formatType) &&
				((Class<?>)cacheEntry.getResourceType()).isAssignableFrom((Class<?>)inputResourceType)) {			
				cachedReaderClass = cacheEntry.getEntryClass();
				break;
			}
		}
		
		return cachedReaderClass;
	}
	
	public Class<?> getCachedWriterClass(Type outputResourceType, String formatType) {
		Class<?> cachedWriterClass = null;
		
		for (CacheEntry cacheEntry : writerClassCache) {
			if (cacheEntry.getFormatType().equalsIgnoreCase(formatType) &&
				((Class<?>)cacheEntry.getResourceType()).isAssignableFrom((Class<?>)outputResourceType)) {			
				cachedWriterClass = cacheEntry.getEntryClass();
				break;
			}
		}
		
		return cachedWriterClass;
	}
	
	public void scan() {
		scan(null);
	}
	
	public void scan(PoiJoiRegistrationListener registrationListener) {
		
		// check that some root packages have been specified!
		
		if (registrationListener != null) {
			registrationListener.registrationStarted();
		}
		
		for (String rootPackageName: rootPackageNames) {
			String resourcePath = createResourceName(rootPackageName);
			
//			logger.info("Checking for classes on resource path {} ", resourcePath);
			
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
						
						String supportedFormatType = classTools.getSupportedFormatType(_class);
						
						if (classTools.isReader(_class)) {
							Class<?> readerClass = _class;
							
							Type genericType = classTools.getReaderGenericType(readerClass);
							CacheEntry cacheEntry = new CacheEntry(readerClass, supportedFormatType, genericType);
							readerClassCache.add(cacheEntry);
							
							logger.info("Found Reader implementation {}", _class.getCanonicalName());
							if (registrationListener != null) {
								registrationListener.registeredExtension(_class.getSimpleName(), _class.getPackage().getName(), "", ExtensionType.READER);
							}
						} else if (classTools.isWriter(_class)) {
							Class<?> writerClass = _class;
							
							Type genericType = classTools.getWriterGenericType(writerClass);
							
							CacheEntry cacheEntry = new CacheEntry(writerClass, supportedFormatType, genericType);
							writerClassCache.add(cacheEntry);
							
							logger.info("Found Writer implementation {}", _class.getCanonicalName());
							if (registrationListener != null) {
								registrationListener.registeredExtension(_class.getSimpleName(), _class.getPackage().getName(), "", ExtensionType.WRITER);
							}
						}
					}				
				}
			} catch (IOException ioException) {
				logger.debug("", ioException);
			}
		}
		
		if (registrationListener != null) {
			registrationListener.registrationFinished();
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
	 * Changes the given package name into a resource name element.
	 * 
	 * @param packageName The package name to convert.
	 * @return The resource name element for the given package name.
	 */
	private String createResourceName(String packageName) {
		return packageName.replace('.', '/') + '/';
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

class CacheEntry {
	
	private Class<?> entryClass;
	private String formatType;
	private Type resourceType;
	
	public CacheEntry(Class<?> entryClass,String formatType, Type resourceType) {
		super();
		this.entryClass = entryClass;
		this.formatType = formatType;
		this.resourceType = resourceType;
	}
	
	public Class<?> getEntryClass() {
		return entryClass;
	}
	
	public String getFormatType() {
		return formatType;
	}
	
	public Type getResourceType() {
		return resourceType;
	}
}	
