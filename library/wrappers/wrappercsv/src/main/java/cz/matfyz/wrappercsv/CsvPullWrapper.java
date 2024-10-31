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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * A pull wrapper implementation for CSV files that implements the {@link AbstractPullWrapper} interface.
 * This class provides methods for pulling data from CSV files and converting it into a {@link ForestOfRecords}.
 */
public class CsvPullWrapper implements AbstractPullWrapper {

    private final CsvProvider provider;

    /**
     * Constructs a new {@code CsvPullWrapper} with the specified CSV provider.
     *
     * @param provider the CSV provider used to access CSV files.
     */
    public CsvPullWrapper(CsvProvider provider) {
        this.provider = provider;
    }

    /**
     * Pulls a forest of records from a CSV file based on a complex property path and query content.
     *
     * @param path the complex property path specifying the structure of the records.
     * @param query the query content used to filter or modify the records pulled.
     * @return a {@link ForestOfRecords} containing the pulled records.
     * @throws PullForestException if an error occurs while pulling the forest of records.
     */
    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        final var forest = new ForestOfRecords();

        try (InputStream inputStream = provider.getInputStream(path.name().toString());
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                RootRecord rootRecord = createRecordFromCSVLine(headers, values, path);
                forest.addRecord(rootRecord);
            }
        } catch (IOException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    /**
     * Creates a {@link RootRecord} from a line of CSV data using the provided headers and values.
     *
     * @param headers an array of header names from the CSV file.
     * @param values an array of values corresponding to a CSV row.
     * @param path the complex property path used to map the CSV data to a record.
     * @return a {@link RootRecord} created from the CSV line.
     */
    private RootRecord createRecordFromCSVLine(String[] headers, String[] values, ComplexProperty path) {
        RootRecord record = new RootRecord();
        for (AccessPath subpath : path.subpaths()) {
            Name name = subpath.name();
            if (name instanceof StaticName) {
                StaticName staticName = (StaticName) name;
                String fieldName = staticName.getStringName();

                int idx = findIndexOf(headers, fieldName);
                String value = values[idx];

                // Assumes that the path has only simple properties (no complex or array) for CSV files (might change later).
                if (subpath instanceof SimpleProperty simpleSubpath) {
                    record.addSimpleValueRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), value);
                }
            }
        }
        return record;
    }

    /**
     * Finds the index of a target string in an array.
     *
     * @param array the array to search in.
     * @param target the string to find.
     * @return the index of the target string, or -1 if not found.
     */
    public static int findIndexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;  // if not found
    }

    /**
     * Converts a {@link Name} object to a {@link RecordName} based on its type (static or dynamic).
     *
     * @param name the name object to convert.
     * @param valueIfDynamic the value to use if the name is dynamic.
     * @return the converted {@link RecordName}.
     */
    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    /**
     * Executes a query statement. This method is currently not implemented.
     *
     * @param statement the query statement to execute.
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("Unimplemented method 'executeQuery'");
    }
}
