package com.karlnosworthy.poijoi.io.writer;

import com.karlnosworthy.poijoi.model.PoijoiMetaData;

/**
 * Interface representing a way of writing a database structure to a given
 * destination
 * 
 * @author john.bartlett
 *
 * @param <T>
 *            The type of destination (e.g. java.io.File, java.sql.Connection
 *            etc)
 */
public interface Writer<T> {

	public enum WriteType {
		DATA_ONLY, SCHEMA_ONLY, BOTH
	}

	/**
	 * Write out to the required output based on the {@link WriteType}
	 * 
	 * @param output
	 *            The destination (e.g. java.io.File, java.sql.Connection etc)
	 * @param metaData
	 *            The database details (e.g. structure and optionally the data)
	 * @param writeType
	 *            Whether to write the Data and/or the Schema
	 *            
	 * @return True if the write was successful otherwise false
	 */
	boolean write(T output, PoijoiMetaData metaData, WriteType writeType)
			throws Exception;
}
