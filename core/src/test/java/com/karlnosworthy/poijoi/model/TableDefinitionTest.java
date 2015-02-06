package com.karlnosworthy.poijoi.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;

public class TableDefinitionTest {

	@Test
	public void testIsSame() {
		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String,ColumnDefinition>();
		columnDefinitions.put("simpleStringColumn", new ColumnDefinition("simpleStringColumn",0,ColumnType.STRING));
		
		TableDefinition tableDefinition = new TableDefinition("simpleTable", columnDefinitions);
		TableDefinition tableDefinition2 = new TableDefinition("simpleTable", columnDefinitions);
		assertTrue(tableDefinition.isSameAs(tableDefinition2));
		assertFalse(tableDefinition.isSameAs(null));
	}	
}
