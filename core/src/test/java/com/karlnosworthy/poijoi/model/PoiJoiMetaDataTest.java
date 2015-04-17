package com.karlnosworthy.poijoi.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;

public class PoiJoiMetaDataTest {

	@Test
	public void testIsSameAs() {
		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String, ColumnDefinition>();
		columnDefinitions.put("ColumnOne", new ColumnDefinition("ColumnOne",0, ColumnType.STRING));
		columnDefinitions.put("ColumnTwo", new ColumnDefinition("ColumnTwo",1, ColumnType.INTEGER_NUMBER));
		
		Map<String, TableDefinition> tableDefinitions = new HashMap<String, TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne", columnDefinitions));
		
		PoiJoiMetaData metaData = new PoiJoiMetaData(false, tableDefinitions, null);
		PoiJoiMetaData metaDataToCompare = new PoiJoiMetaData(false, tableDefinitions, null);
		
		assertTrue(metaData.isSameAs(metaDataToCompare));
		assertFalse(metaData.isSameAs(null));
	}
}
