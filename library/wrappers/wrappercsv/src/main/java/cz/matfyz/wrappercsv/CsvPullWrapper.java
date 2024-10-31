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
import cz.matfyz.core.mapping.StaticName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

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

        final CsvSchema baseSchema = CsvSchema.emptySchema()
            .withColumnSeparator(provider.getSeparator())
            .withEscapeChar('\\');

        final CsvSchema schema = provider.hasHeader()
            ? baseSchema.withHeader()
            : baseSchema.withColumnsFrom(createHeaderSchema(path));

        try (
            InputStream inputStream = provider.getInputStream(path.name().toString());
        ) {
            final MappingIterator<Map<String, String>> reader = new CsvMapper()
                .readerFor(Map.class)
                .with(schema)
                .readValues(inputStream);

            while (reader.hasNext())
                forest.addRecord(createRecord(path, reader.nextValue()));
        } catch (IOException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    private CsvSchema createHeaderSchema(ComplexProperty path) {
        // If no header is provided, we have to hope the columns are in the correct order ...
        final var builder = CsvSchema.builder();
        path.subpaths().stream()
            .map(property -> ((StaticName) property.name()).getStringName())
            .forEach(builder::addColumn);

        return builder.build();
    }

    private RootRecord createRecord(ComplexProperty path, Map<String, String> line) {
        final RootRecord record = new RootRecord();
        for (final AccessPath property : path.subpaths()) {
            final var name = ((StaticName) property.name());
            final String value = line.get(name.getStringName());
            record.addSimpleValueRecord(name.toRecordName(), property.signature(), value);
        }
        return record;
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
