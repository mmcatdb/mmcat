package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.record.RecordName;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.record.ComplexRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.Arrays;

import org.json.JSONObject;


public class CsvPullWrapper implements AbstractPullWrapper {

    private final CsvProvider provider;

    public CsvPullWrapper(CsvProvider provider) {
        this.provider = provider;
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        //System.out.println("csv pullwrapper");
        //System.out.println("query: " + query);
        final var forest = new ForestOfRecords();

        try (InputStream inputStream = provider.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println("Line to be processed: " + line);
                String[] values = line.split(",");
                RootRecord rootRecord = createRecordFromCSVLine(headers, values, path);
                forest.addRecord(rootRecord);
            }
        } catch (IOException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    private RootRecord createRecordFromCSVLine(String[] headers, String[] values, ComplexProperty path) {
        RootRecord record = new RootRecord();
        for (AccessPath subpath : path.subpaths()) {
            Name name = subpath.name();
            if (name instanceof StaticName) {
                StaticName staticName = (StaticName) name;
                String fieldName = staticName.getStringName();

                int idx = findIndexOf(headers, fieldName);
                String value = values[idx];

                // we assume that path has only simple properties (no complex or array) for csv files (might change later)
                if (subpath instanceof SimpleProperty simpleSubpath) {
                    record.addSimpleValueRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), value);
                }
            }
        }
        return record;
    }

    public static int findIndexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;  // if not found
    }


    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    @Override
    public QueryResult executeQuery(QueryStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeQuery'");
    }

    @Override public JSONObject getTableNames(String limit, String offset) {
        throw new UnsupportedOperationException("CsvPullWrapper.getTableNames not implemented.");
    }

    @Override public JSONObject getTable(String tableName, String limit, String offset) {
        throw new UnsupportedOperationException("CsvPullWrapper.gatTable not implemented.");
    }

    @Override public JSONObject getRows(String tableName, String columnName, String columnValue, String operator, String limit, String offset) {
        throw new UnsupportedOperationException("CsvPullWrapper.getRow not implemented.");
    }

}
