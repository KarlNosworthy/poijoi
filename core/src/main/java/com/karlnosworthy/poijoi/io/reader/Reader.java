package com.karlnosworthy.poijoi.io.reader;

import com.karlnosworthy.poijoi.model.PoijoiMetaData;

/**
 * Interface representing a way of reading in a database structure
 * from a given source
 *  
 * @author john.bartlett
 *
 * @param <T>
 *            The type of source (e.g. java.io.File, java.sql.Connection etc)
 */
public interface Reader<T> {

	/**
	 * Read a source to build a Poijoi meta data object based on the read type
	 * 
	 * @param source The source input for the data
	 * @param readData Whether or not to read the data or just the structure
	 * 
	 */
	PoijoiMetaData read(T source, boolean readData) throws Exception;

}
