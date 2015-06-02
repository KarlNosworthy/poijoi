package com.karlnosworthy.poijoi.io.reader.spreadsheet;

import com.karlnosworthy.poijoi.model.IndexDefinition;

import java.util.List;
import java.util.Map;

/**
 * Created by karlnosworthy on 02/06/15.
 */
public abstract class SpreadsheetIndexDefinitionReader<W,S,R> {

    protected final int CELL_INDEX_NAME = 0;
    protected final int CELL_TABLE_NAME = 1;
    protected final int CELL_TABLE_COLUMNS = 2;
    protected final int CELL_UNIQUE_FLAG = 3;


    public abstract S findIndexDefinitionSheet(W workbook);

    public abstract Map<String, List<IndexDefinition>> readDefinitions(S sheet);

    public abstract IndexDefinition readDefinition(R row);


    public boolean isIndexSheet(String sheetName) {
        if (sheetName.equalsIgnoreCase("indexes") ||
            sheetName.equalsIgnoreCase("indices")) {
            return true;
        }
        return false;
    }
}

