package com.karlnosworthy.poijoi.io.reader.spreadsheet;

import com.karlnosworthy.poijoi.model.ColumnDefinition;
import com.karlnosworthy.poijoi.model.IndexDefinition;
import com.karlnosworthy.poijoi.model.TableDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class SpreadsheetTableDefinitionReader<S> {

    public abstract TableDefinition read(S sheet);

    public abstract TableDefinition read(S sheet, Map<String,List<IndexDefinition>> indexDefinitions);

}