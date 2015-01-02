package com.karlnosworthy.poijoi.model;

public class ColumnDefinition {

	public enum ColumnType {
		STRING, DATE, INTEGER_NUMBER, DECIMAL_NUMBER
	}

	private String columnName;

	private int columnIndex;

	private ColumnType columnType;

	public ColumnDefinition(String columnName, int columnIndex,
			ColumnType columnType) {
		super();
		this.columnName = columnName;
		this.columnIndex = columnIndex;
		this.columnType = columnType;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

}
