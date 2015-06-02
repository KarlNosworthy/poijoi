package com.karlnosworthy.poijoi.io.reader.xls;

import com.karlnosworthy.poijoi.io.reader.spreadsheet.SpreadsheetTableDefinitionReader;
import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by karlnosworthy on 02/06/15.
 */
public class XLSTableDefinitionReader extends SpreadsheetTableDefinitionReader<Sheet> {

    private DataFormatter dataFormatter;


    public XLSTableDefinitionReader() {
        super();
        this.dataFormatter = new DataFormatter();
    }

    public TableDefinition read(Sheet sheet) {
        return read(sheet, null);
    }

    public TableDefinition read(Sheet sheet, Map<String,List<IndexDefinition>> indexDefinitions) {

        TableDefinition tableDefinition = null;

        // Find header column
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());

        String tableName = sheet.getSheetName();

        // If we don't have any columns then there's nothing we can do
        if (headerRow != null) {
            Row dataRow = sheet.getRow(1 + sheet.getFirstRowNum());

            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                Cell headerRowCell = headerRow.getCell(cellIndex);
                String cellName = headerRowCell.getStringCellValue();

                Cell typedRowCell = null;
                if (dataRow != null) {
                    typedRowCell = dataRow.getCell(cellIndex);
                }

                ColumnDefinition.ColumnType columnType = ColumnDefinition.ColumnType.STRING;

                int cellType = Cell.CELL_TYPE_BLANK;
                if (typedRowCell != null) {
                    cellType = typedRowCell.getCellType();
                }

                switch (cellType) {
                    case Cell.CELL_TYPE_BLANK:
                        if (cellName.endsWith(".id")) {
                            columnType = ColumnDefinition.ColumnType.INTEGER_NUMBER;
                        } else {
                            columnType = ColumnDefinition.ColumnType.STRING;
                        }
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                    case Cell.CELL_TYPE_ERROR:
                    case Cell.CELL_TYPE_STRING:
                        columnType = ColumnDefinition.ColumnType.STRING;
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                    case Cell.CELL_TYPE_NUMERIC:
                        if (HSSFDateUtil.isCellDateFormatted(typedRowCell)) {
                            columnType = ColumnDefinition.ColumnType.DATE;
                        } else {
                            String formattedValue = dataFormatter
                                    .formatCellValue(typedRowCell);

                            if (formattedValue.contains(".")) {
                                columnType = ColumnDefinition.ColumnType.DECIMAL_NUMBER;
                            } else {
                                columnType = ColumnDefinition.ColumnType.INTEGER_NUMBER;
                            }
                        }
                        break;
                }

                columns.add(new ColumnDefinition(cellName, cellIndex, columnType));
            }

            if (indexDefinitions != null && indexDefinitions.containsKey(tableName)) {
                tableDefinition = new TableDefinition(tableName, columns, indexDefinitions.get(tableName));
            } else {
                tableDefinition = new TableDefinition(tableName, columns);
            }
        }
        return tableDefinition;
    }
}
