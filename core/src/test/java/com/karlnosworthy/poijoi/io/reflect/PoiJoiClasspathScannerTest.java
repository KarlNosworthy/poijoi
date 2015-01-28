package com.karlnosworthy.poijoi.io.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PoiJoiClasspathScannerTest {

	private PoiJoiClasspathScanner classpathScanner;
	
	@Before
	public void onSetup() {
		classpathScanner = new PoiJoiClasspathScanner();
	}
	
	@After
	public void onTearDown() {
		classpathScanner = null;
	}
	
	@Test
	public void testRootPackageNames() {
		assertNotNull(classpathScanner);
		assertEquals(0, classpathScanner.getRootPackageNames().size());
		
		classpathScanner.addRootPackageName("com.custom.package.name");
		
		assertEquals(1, classpathScanner.getRootPackageNames().size());
		assertEquals("com.custom.package.name", classpathScanner.getRootPackageNames().get(0));
	}
}
