package com.karlnosworthy.poijoi;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.karlnosworthy.poijoi.io.FormatType;
import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;

public class PoiJoiManager {
	
	private Set<Class<?>> readerClassCache;
	private HashMap<FormatType, Reader<?>> readerInstanceCache;
	private Set<Class<?>> writerClassCache;
	private HashMap<FormatType, Writer<?>> writerInstanceCache;

	public PoiJoiManager() throws IOException {
		super();
		findAndCacheClassesOnClasspath();
	}
	
	public <T> Reader<T> findReader(T input, FormatType formatType) throws IOException {
		return getCachedReader(input, formatType);
	}
	
	public <T> Writer<T> findWriter(T output, FormatType formatType) {
		return getCachedWriter(output, formatType);
	}	
	
	private <T> Reader<T> getCachedReader(T input, FormatType formatType) {
		Reader<T> reader = null;
		
		if (readerInstanceCache != null && !readerInstanceCache.isEmpty()) {
			if (readerInstanceCache.containsKey(formatType)) {
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
							reader = (Reader) readerClass.newInstance();
							if (readerInstanceCache == null) {
								readerInstanceCache = new HashMap<FormatType,Reader<?>>();
							}
							readerInstanceCache.put(formatType, reader);
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
	
	private boolean supportsFormat(Class _class, FormatType formatType) {
		Annotation annotation = (Annotation) _class.getAnnotation(SupportsFormat.class);
		if (annotation instanceof SupportsFormat) {
			SupportsFormat supportsFormatAnnotation = (SupportsFormat) annotation;
			if (supportsFormatAnnotation.type() == formatType) {
				return true;
			}
		}
		return false;
	}
	
	private <T> Writer<T> getCachedWriter(T output, FormatType formatType) {
		Writer<T> writer = null;
		
		if (writerInstanceCache != null && !writerInstanceCache.isEmpty()) {
			if (writerInstanceCache.containsKey(formatType)) {
				writer = (Writer<T>) writerInstanceCache.get(formatType);
			}
		}
		
		if (writer == null) {
			if (writerClassCache != null && !writerClassCache.isEmpty()) {
				
				Iterator<Class<?>> writerClassIterator = writerClassCache.iterator();
				
				while (writerClassIterator.hasNext()) {
					Class<?> writerClass =  (Class<?>) writerClassIterator.next();
					
					if (supportsFormat(writerClass, formatType)) {
						try {
							writer = (Writer) writerClass.newInstance();
							if (writerInstanceCache == null) {
								writerInstanceCache = new HashMap<FormatType,Writer<?>>();
							}
							writerInstanceCache.put(formatType, writer);
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
	
	private FormatType determineFormatType(String qualifier) {
		FormatType formatType = null;
		
		if (isFile(qualifier)) {
			File qualifierFile = new File(qualifier);
			
			String qualifierFilePath = qualifierFile.getAbsolutePath();

			if (qualifierFilePath.endsWith(FormatType.XLS.name().toLowerCase())) {
				formatType = FormatType.XLS;
			} else if (qualifierFilePath.endsWith(FormatType.XLSX.name().toLowerCase())) {
				formatType = FormatType.XLSX;
			} else if (qualifierFilePath.endsWith(FormatType.ODS.name().toLowerCase())) {
				formatType = FormatType.ODS;
			}
		} else if (isJdbcURL(qualifier)) {
			int procotolEndIndex = (1 + qualifier.indexOf(":"));
			int subProtocolEndIndex = qualifier.indexOf(":", procotolEndIndex);
			
			String subProtocol = qualifier.substring(procotolEndIndex, subProtocolEndIndex);
			
			if (subProtocol.equalsIgnoreCase(FormatType.SQLITE.name())) {
				formatType = FormatType.SQLITE;
			}
		}
		
		return formatType;
	}
	
	private void findAndCacheClassesOnClasspath() throws IOException {
		Map<String, Set<Class<?>>> foundClasses = findAll(getClass().getClassLoader(), getClass().getPackage().getName());
		this.readerClassCache = foundClasses.get(Reader.class.getName());
		this.writerClassCache = foundClasses.get(Writer.class.getName());
	}
	
	
	private Map<String, Set<Class<?>>> findAll(ClassLoader classLoader, String rootPackage) throws IOException {

		Set<Class<?>> readerClasses = new HashSet<Class<?>>();
		Set<Class<?>> writerClasses = new HashSet<Class<?>>();
		
		
		Set<Class<?>> classes = new HashSet<Class<?>>();

		String resourcePath = createResourceName(rootPackage);
		Enumeration<URL> resourceURLs = getClass().getClassLoader().getResources(resourcePath);
		while (resourceURLs.hasMoreElements()) {
			URL resourceURL = resourceURLs.nextElement();

			if (isJarURL(resourceURL)) {
				URLConnection con = resourceURL.openConnection();
				JarFile jarFile = null;

				if (con instanceof JarURLConnection) {
					JarURLConnection jarCon = (JarURLConnection) con;
					jarCon.setUseCaches(false);
					jarFile = jarCon.getJarFile();
					
					classes.addAll(findClassesInEntries(rootPackage, jarFile, classLoader));
				}
			} else {
				File[] subDirectories = obtainSubDirectoryNames(new File(resourceURL.getFile()));
				
				for (File subDirectory : subDirectories) {
					File[] potentialClassFilenames = obtainClassFilenames(subDirectory);
					
					String packageName = makePackageName(rootPackage, subDirectory.getName(), true);
					for (File potentialClassFilename : potentialClassFilenames) {
						try {
							classes.add(Class.forName(packageName + potentialClassFilename.getName().replace(".class", "")));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		for (Class<?> _class : classes) {
			if (isValidReaderImplementation(_class)) {
				readerClasses.add(_class);
			} else if (isValidWriterImplementation(_class)) {
				writerClasses.add(_class);
			}
		}
		
		Map<String, Set<Class<?>>> foundClasses = new HashMap<String, Set<Class<?>>>();
		foundClasses.put(Reader.class.getName(), readerClasses);
		foundClasses.put(Writer.class.getName(), writerClasses);
		
		return foundClasses;
	}	
	
	private Set<Class<?>> findClassesInEntries(String rootPackageName, JarFile jarFile, ClassLoader classLoader) {
		Set<Class<?>> classes = new HashSet<Class<?>>();

		String resourceName = createResourceName(rootPackageName);
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			String entryPath = entry.getName();

			if (entryPath.startsWith(resourceName) && entryPath.endsWith(".class")) {
				String className = createPackageName(entryPath).replace(".class", "");
				try {
					Class<?> _clazz = classLoader.loadClass(className);
					classes.add(_clazz);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return classes;
	}
	
	private boolean isValidReaderImplementation(Class<?> _class) {
		if (Reader.class.isAssignableFrom(_class) && !_class.isInterface()) {
			return true;
		}
		return false;
	}
	
	private boolean isValidWriterImplementation(Class<?> _class) {
		if (Writer.class.isAssignableFrom(_class) && !_class.isInterface()) {
			return true;
		}
		return false;
	}
	
	private String createResourceName(String packageName) {
		return (packageName.replace('.', '/') + '/');
	}

	private String createPackageName(String resourceName) {
		return resourceName.replace('/', '.');
	}

	private boolean isJarURL(URL url) {
		return url.getProtocol().equals("jar");
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
	
	private File[] obtainClassFilenames(File rootDir) {
		return rootDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".class") && name.indexOf("$") < 0 &&
					!name.equals("Reader.class") && !name.equals("Writer.class") &&
					!name.endsWith("Test.class")) {
					return true;
				}
				return false;
			}
		});
	}
}
