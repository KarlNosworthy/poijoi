package com.karlnosworthy.poijoi.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;

public class ColumnDefinitionTest {
	
	@Test
	public void testIsSame() {
		ColumnDefinition columnDefinition1 = new ColumnDefinition("simpleColumn", 0, ColumnType.STRING);
		ColumnDefinition columnDefinition2 = new ColumnDefinition("simpleColumn", 0, ColumnType.STRING);
		assertTrue(columnDefinition1.isSameAs(columnDefinition2));
		assertFalse(columnDefinition1.isSameAs(null));
		
		columnDefinition2 = new ColumnDefinition("anotherColumn", 0, ColumnType.STRING);
		assertFalse(columnDefinition1.isSameAs(columnDefinition2));
		
		columnDefinition2 = new ColumnDefinition("simpleColumn", 1, ColumnType.STRING);
		assertFalse(columnDefinition1.isSameAs(columnDefinition2));
		
		columnDefinition2 = new ColumnDefinition("simpleColumn", 0, ColumnType.INTEGER_NUMBER);
		assertFalse(columnDefinition1.isSameAs(columnDefinition2));
	}
}
