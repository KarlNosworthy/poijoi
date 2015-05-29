package com.karlnosworthy.poijoi.model;

/**
 * Representation of a database column
 * 
 * @author john.bartlett
 *
 */
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
	
	public boolean isSameAs(ColumnDefinition columnDefinition) {
		if (columnDefinition != null && columnName != null &&
			columnDefinition.columnName != null &&
			columnName.equals(columnDefinition.columnName) && 
			columnType == columnDefinition.columnType &&
			columnIndex == columnDefinition.columnIndex) {
				return true;
		}
		return false;
	}

	public boolean isIDColumn() {
		if (columnName != null && !columnName.isEmpty() &&
			(columnName.equalsIgnoreCase("id") ||
			 columnName.toLowerCase().endsWith(".id"))) {
			return true;
		}
		return false;
	}

	public boolean isRelationshipIDColumn() {
		if (columnName != null && !columnName.isEmpty() &&
			columnName.toLowerCase().endsWith(".ids")) {
			return true;
		}
		return false;
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("name:");
		stringBuilder.append(columnName);
		stringBuilder.append(",index:");
		stringBuilder.append(columnIndex);
		stringBuilder.append(",type:");
		stringBuilder.append(columnType.name());
		stringBuilder.append(",id:");
		stringBuilder.append(isIDColumn());
		stringBuilder.append(",link id(s):");
		stringBuilder.append(isRelationshipIDColumn());
		return stringBuilder.toString();
	}
}
