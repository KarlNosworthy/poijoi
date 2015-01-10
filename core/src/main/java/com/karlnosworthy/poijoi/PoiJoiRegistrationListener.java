package com.karlnosworthy.poijoi;

/**
 * This listener can be used to obtain notifications from a {@link PoiJoi}
 * instance that will be scanning the classpath for available {@link Reader}
 * and {@link Writer} implementations.
 * 
 * @author Karl Nosworthy
 * @version 1.0
 * @since 1.0
 */
public interface PoiJoiRegistrationListener {
	
	public enum ExtensionType {
		READER,
		WRITER
	};
	
	/**
	 * Invoked when the {@link PoiJoi} instance is about to start scanning and registering. 
	 */
	public void registrationStarted();
	
	/**
	 * Invoked when the {@link PoiJoi} instance has found an available extension class
	 * and has registered it to be available once the scanning has completed.
	 * 
	 * @param className The simple name of the class that has been found.
	 * @param packageName The package in which the class is located.
	 * @param formatType The format type that the class supports.
	 * @param extensionType The type of extension the class provides.
	 */
	public void registeredExtension(String className, String packageName, String formatType, ExtensionType extensionType);
	
	/**
	 * Invoked when the {@link PoiJoi} instance has completed its scanning and registering.
	 */
	public void registrationFinished();
}
