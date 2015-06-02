package com.karlnosworthy.poijoi.io.reader.xls;

import com.karlnosworthy.poijoi.io.reader.spreadsheet.SpreadsheetIndexDefinitionReader;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by karlnosworthy on 02/06/15.
 */
public class XLSIndexDefinitionReader extends SpreadsheetIndexDefinitionReader<Workbook, Sheet, Row> {

    public XLSIndexDefinitionReader() {
        super();
    }

    @Override
    public Sheet findIndexDefinitionSheet(Workbook workbook) {

        if (workbook != null && workbook.getNumberOfSheets() > 0) {
            for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                String sheetName = workbook.getSheetName(sheetNumber);
                if (isIndexSheet(sheetName)) {
                    return workbook.getSheetAt(sheetNumber);
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, List<IndexDefinition>> readDefinitions(Sheet sheet) {
        Map<String, List<IndexDefinition>> tableIndexDefinitions = new HashMap<String, List<IndexDefinition>>();

        if (sheet.getFirstRowNum() == sheet.getLastRowNum()) {
            IndexDefinition indexDefinition = readDefinition(sheet.getRow(0));

            if (indexDefinition != null) {
                List<IndexDefinition> indexDefinitions = new ArrayList<IndexDefinition>();
                indexDefinitions.add(indexDefinition);

                tableIndexDefinitions.put(indexDefinition.getTableName(), indexDefinitions);
            }
        } else {
            int numberOfIndexes = sheet.getLastRowNum() - sheet.getFirstRowNum();

            for (int rowNumber = sheet.getFirstRowNum(); rowNumber < numberOfIndexes; rowNumber++) {
                IndexDefinition indexDefinition = readDefinition(sheet.getRow(rowNumber));

                if (indexDefinition != null) {
                    List<IndexDefinition> indexDefinitions = null;

                    if (tableIndexDefinitions.containsKey(indexDefinition.getTableName())) {
                        indexDefinitions = tableIndexDefinitions.get(indexDefinition.getTableName());
                    } else {
                        indexDefinitions = new ArrayList<IndexDefinition>();
                        tableIndexDefinitions.put(indexDefinition.getTableName(), indexDefinitions);
                    }

                    indexDefinitions.add(indexDefinition);
                }
            }
        }

        return tableIndexDefinitions;
    }

    @Override
    public IndexDefinition readDefinition(Row row) {

        IndexDefinition indexDefinition = null;

        String indexName = getValueForCell(row, CELL_INDEX_NAME);
        String tableName = getValueForCell(row, CELL_TABLE_NAME);
        String columns = getValueForCell(row, CELL_TABLE_COLUMNS);

        String[] columnNames=  columns.split(",");

        indexDefinition = new IndexDefinition(indexName, tableName, columnNames, false);

        return indexDefinition;
    }

    private String getValueForCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return cell.getStringCellValue();
    }
}
