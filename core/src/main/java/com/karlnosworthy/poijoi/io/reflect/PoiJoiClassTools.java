package com.karlnosworthy.poijoi.io.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;

/**
 * A class that collects together a number of utility methods to help determine
 * if individual classes can be considered compatible PoiJoi {@link Reader} and {@link Writer}
 * instances.
 * 
 * @author Karl Nosworthy
 * @since 1.0
 */
public class PoiJoiClassTools {

	/**
	 * Checks if the specified class implements the PoiJoi {@link Reader} interface and
	 * contains a valid {@link SupportsFormat} annotation.
	 * 
	 * @param _class The class to check 
	 * @return True if the class can be used as a PoiJoi Reader implementation.
	 */
	public boolean isReader(Class<?> _class) {
		if (_class != null) {
			return Reader.class.isAssignableFrom(_class)  && containsValidSupportsFormatAnnotation(_class);
		}
		return false;
	}
	
	/**
	 * Attempts to instantiate a PoiJoi {@link Reader} instance which supports the given
	 * input type from the specified class.
	 * 
	 * @param input The type of input that the reader is required to support
	 * @param _class The class to instantiate as a reader instance.
	 * @return A reader instance or null
	 */
	@SuppressWarnings("unchecked")
	public <T> Reader<T> createReaderInstance(T input, Class<?> _class) throws IllegalAccessException, InstantiationException {
		Reader<T> reader = null;
		if (isReader(_class)) {
			reader = (Reader<T>) _class.newInstance();
		}
		
		return reader;
	}
	
	/**
	 * Checks if the specified class implements the PoiJoi {@link Writer} interface and
	 * contains a valid {@link SupportsFormat} annotation.
	 * 
	 * @param _class The class to check 
	 * @return True if the class can be used as a PoiJoi Writer implementation.
	 */
	public boolean isWriter(Class<?> _class) {
		if (_class != null) {
			return Writer.class.isAssignableFrom(_class) && containsValidSupportsFormatAnnotation(_class);
		}
		return false;
	}
	
	/**
	 * Attempts to instantiate a PoiJoi {@link Writer} instance which supports the given
	 * output type from the specified class.
	 * 
	 * @param output The type of output that the writer is required to support
	 * @param _class The class to instantiate as a writer instance.
	 * @return A writer instance or null
	 */
	@SuppressWarnings("unchecked")
	public <T> Writer<T> createWriterInstance(T output, Class<?> _class) throws IllegalAccessException, InstantiationException{
		Writer<T> writer = null;
		if (isWriter(_class)) {
			writer = (Writer<T>) _class.newInstance();
		}
		return writer;
	}	
	
	/**
	 * Checks if the specified class contains a {@link SupportsFormat} annotation 
	 * which also has a populated 'type' attribute.
	 * 
	 * @param _class The class to check
	 * @return True if a correctly configured annotation is present otherwise false.
	 */
	public boolean containsValidSupportsFormatAnnotation(Class<?> _class) {
		if (_class != null) {
			String supportedFormatType = getSupportedFormatType(_class);
			if (supportedFormatType != null && supportedFormatType.trim().length() > 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the format type supported by the specified class.
	 * 
	 * @param _class The class to check.
	 * @return A string containing the support format type or null if one cannot be found.
	 */
	public String getSupportedFormatType(Class<?> _class) {
		String supportedFormat = null;
		
		if (_class != null && _class.isAnnotationPresent(SupportsFormat.class)) {
			SupportsFormat supportsFormatAnnotation = _class.getAnnotation(SupportsFormat.class);
			if (supportsFormatAnnotation != null) {
				supportedFormat = supportsFormatAnnotation.type();
			}
		}
		return supportedFormat;
	}
	
	/**
	 * 
	 * @param formatType
	 * @param genericTypeClass
	 * @return
	 */
	public String makeKey(String formatType, Class<?> genericTypeClass) {
		if (formatType != null && formatType.trim().length() > 0 && genericTypeClass != null) {
			return formatType.toUpperCase() + genericTypeClass.getCanonicalName();
		}
		return null;
	}
	
	
	public Type getReaderGenericType(Class<?> _class) {
		
		Type readerGenericType = null;
		
		if (_class != null) {
			Type[] genericInterfaces = _class.getGenericInterfaces();
			for (Type genericInterfaceType : genericInterfaces) {
				if (genericInterfaceType instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) genericInterfaceType;
					if (parameterizedType.getRawType() == Reader.class) {
						readerGenericType = parameterizedType.getActualTypeArguments()[0];
					}
					break;
				}
			}
			
			if (readerGenericType == null) {
				for (Type genericInterfaceType : genericInterfaces) {
					if (genericInterfaceType instanceof ParameterizedType) {
						ParameterizedType parameterizedType = (ParameterizedType) genericInterfaceType;
						readerGenericType = getReaderGenericType((Class<?>)parameterizedType.getRawType());
						
						if (readerGenericType != null) {
							break;
						}
					} else {
						readerGenericType = getReaderGenericType((Class<?>)genericInterfaceType);
					}
				}
				if (readerGenericType == null) {
					readerGenericType = getReaderGenericType(_class.getSuperclass());
				}
			}
		}
		
		return readerGenericType;
	}
	
	
	public Type getWriterGenericType(Class<?> _class) {
		
		Type writerGenericType = null;
		
		if (_class != null) {
			Type[] genericInterfaces = _class.getGenericInterfaces();
			for (Type genericInterfaceType : genericInterfaces) {
				if (genericInterfaceType instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) genericInterfaceType;
					if (parameterizedType.getRawType() == Writer.class) {
						writerGenericType = parameterizedType.getActualTypeArguments()[0];
					}
					break;
				}
			}
			
			if (writerGenericType == null) {
				for (Type genericInterfaceType : genericInterfaces) {
					if (genericInterfaceType instanceof ParameterizedType) {
						ParameterizedType parameterizedType = (ParameterizedType) genericInterfaceType;
						writerGenericType = getWriterGenericType((Class<?>)parameterizedType.getRawType());
						
						if (writerGenericType != null) {
							break;
						}
					} else {
						writerGenericType = getWriterGenericType((Class<?>)genericInterfaceType);
					}
				}
				if (writerGenericType == null) {
					writerGenericType = getWriterGenericType(_class.getSuperclass());
				}
			}
		}
		
		return writerGenericType;
	}	
			
			
/*
 * Start at a class and check what interfaces it implements
 *   Check if any of those are the correct one
 *      if so get the generic type from there
 *         and return it
 *      if not check what each interface in turn implements
 *         if one of those extend the correct one
 *            get the generic type and return it
 *      if not obtain the superclass of the current class and start again...
 * 			
 */
			
	
}

