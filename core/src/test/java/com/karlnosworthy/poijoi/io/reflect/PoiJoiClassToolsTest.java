package com.karlnosworthy.poijoi.io.reflect;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.karlnosworthy.poijoi.io.SupportsFormat;
import com.karlnosworthy.poijoi.io.reader.Reader;
import com.karlnosworthy.poijoi.io.writer.Writer;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;

public class PoiJoiClassToolsTest {
	
	private PoiJoiClassTools classTools;
	
	@Before
	public void onSetup() {
		classTools = new PoiJoiClassTools();
	}
	
	@After
	public void onTearDown() {
		classTools = null;
	}
	
	@Test
	public void testIsReader() {
		assertFalse(classTools.isReader(null));
		assertFalse(classTools.isReader(classTools.getClass()));
		assertTrue(classTools.isReader(SimpleReader.class));
		assertFalse(classTools.isReader(SimpleWriter.class));
	}
	
	@Test
	public void testIsWriter() {
		assertFalse(classTools.isWriter(null));
		assertFalse(classTools.isWriter(classTools.getClass()));
		assertFalse(classTools.isWriter(SimpleReader.class));
		assertTrue(classTools.isWriter(SimpleWriter.class));
	}
	
	@Test
	public void testContainsValidSupportFormatAnnotation() {
		assertFalse(classTools.containsValidSupportsFormatAnnotation(null));
		assertFalse(classTools.containsValidSupportsFormatAnnotation(classTools.getClass()));
		assertTrue(classTools.containsValidSupportsFormatAnnotation(SimpleReader.class));
		assertTrue(classTools.containsValidSupportsFormatAnnotation(SimpleWriter.class));
	}
	
	@Test
	public void testGetSupportedFormatType() {
		assertNull(classTools.getSupportedFormatType(null));
		assertNull(classTools.getSupportedFormatType(classTools.getClass()));
		assertEquals("DUMMY", classTools.getSupportedFormatType(SimpleReader.class));
		assertEquals("DUMMY", classTools.getSupportedFormatType(SimpleWriter.class));
	}
	
	@Test
	public void testCreateReaderInstance() throws Exception {
		assertNull(classTools.createReaderInstance(null, null));
		assertNull(classTools.createReaderInstance(new File(""), classTools.getClass()));
		assertNotNull(classTools.createReaderInstance(new File(""), SimpleReader.class));
		assertNull(classTools.createReaderInstance(new File(""), SimpleWriter.class));
	}

	@Test
	public void testCreateWriterInstance() throws Exception {
		assertNull(classTools.createWriterInstance(null, null));
		assertNull(classTools.createWriterInstance(new File(""), classTools.getClass()));
		assertNull(classTools.createWriterInstance(new File(""), SimpleReader.class));
		assertNotNull(classTools.createWriterInstance(new File(""), SimpleWriter.class));
	}
	
	@Test
	public void testMakeKey() {
		assertNull(classTools.makeKey(null, null));
		assertNull(classTools.makeKey("", null));
		assertEquals("DUMMYjava.io.File", classTools.makeKey("DUMMY", File.class));
	}
	
	@Test
	public void testGetReaderGenericType() {
		assertNull(null, classTools.getReaderGenericType(null));
		assertEquals(File.class, classTools.getReaderGenericType(SimpleReader.class));
	}
	
	@Test
	public void testGetWriterGenericType() {
		assertNull(null, classTools.getWriterGenericType(null));
		assertEquals(File.class, classTools.getWriterGenericType(SimpleWriter.class));
	}
}

@SupportsFormat(type = "DUMMY")	
class SimpleReader implements Reader<File> {

	@Override
	public PoijoiMetaData read(File input, boolean readData) throws Exception {
		return null;
	}
}

@SupportsFormat(type = "DUMMY")	
class SimpleWriter implements Writer<File> {

	@Override
	public boolean write(File output, PoijoiMetaData metaData, WriteType writeType) throws Exception {
		return false;
	}
}
