package com.karlnosworthy.poijoi.io.writer.sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.karlnosworthy.poijoi.io.writer.Writer.WriteType;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.ColumnDefinition.ColumnType;
import com.karlnosworthy.poijoi.model.PoijoiMetaData;
import com.karlnosworthy.poijoi.model.TableDefinition;

public class SQLiteDatabaseWriterTest {

	@Test
	public void testWrite() throws Exception {
		Map<String, ColumnDefinition> columnDefinitions = new HashMap<String, ColumnDefinition>();
		columnDefinitions.put("col1String", new ColumnDefinition("col1String",
				0, ColumnType.STRING));
		columnDefinitions.put("col2Date", new ColumnDefinition("col2Date", 1,
				ColumnType.DATE));
		columnDefinitions.put("col3Integer", new ColumnDefinition(
				"col3Integer", 2, ColumnType.INTEGER_NUMBER));
		columnDefinitions.put("col4Decimal", new ColumnDefinition(
				"col4Decimal", 3, ColumnType.DECIMAL_NUMBER));

		Map<String, TableDefinition> tableDefinitions = new HashMap<String, TableDefinition>();
		tableDefinitions.put("TableOne", new TableDefinition("TableOne",
				columnDefinitions));

		HashMap<String, Object> row1Data = new HashMap<String, Object>();
		row1Data.put("col1String", "hello");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 12, 31, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		row1Data.put("col2Date", cal.getTime());
		row1Data.put("col3Integer", new Integer(19));
		row1Data.put("col4Decimal", new Double("1.5"));

		List<HashMap<String, Object>> rowData = new ArrayList<HashMap<String, Object>>();
		rowData.add(row1Data);

		Map<String, List<HashMap<String, Object>>> tableData = new HashMap<String, List<HashMap<String, Object>>>();
		tableData.put("TableOne", rowData);

		PoijoiMetaData metaData = new PoijoiMetaData(true, tableDefinitions,
				tableData);
		assertTrue(metaData.getTableData().size() == 1);
		assertTrue(metaData.getTableDefinitions().size() == 1);

		SQLiteDatabaseWriter writer = new SQLiteDatabaseWriter();
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "test.sqlite");
		file.deleteOnExit();

		// validate contents of the file
		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ file.getAbsolutePath());

			writer.write(connection, metaData, WriteType.BOTH);

			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet tablesResultSet = databaseMetaData.getTables(null, null,
					null, new String[] { "TABLE" });

			int tableCount = 0;
			while (tablesResultSet.next()) {
				tableCount += 1;
			}
			tablesResultSet.close();

			assertEquals(2, tableCount);

			Statement statement = connection.createStatement();
			ResultSet tableRowCountResultSet = statement
					.executeQuery("select count(*) from TableOne");

			assertTrue(tableRowCountResultSet.next());
			assertEquals(1, tableRowCountResultSet.getInt(1));

			tableRowCountResultSet.close();

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
}
