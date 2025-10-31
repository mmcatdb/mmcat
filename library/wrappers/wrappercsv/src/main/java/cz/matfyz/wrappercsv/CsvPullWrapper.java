package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * A pull wrapper implementation for CSV files that implements the {@link AbstractPullWrapper} interface.
 * This class provides methods for pulling data from CSV files and converting it into a {@link ForestOfRecords}.
 */
public class CsvPullWrapper implements AbstractPullWrapper {

    private final CsvProvider provider;

    /**
     * Constructs a new {@code CsvPullWrapper} with the specified CSV provider.
     */
    public CsvPullWrapper(CsvProvider provider) {
        this.provider = provider;
    }

    /**
     * Pulls a forest of records from a CSV file based on a complex property path and query content.
     */
    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        final var forest = new ForestOfRecords();

        final CsvSchema baseSchema = CsvSchema.emptySchema()
            .withColumnSeparator(provider.settings.separator())
            .withEscapeChar('\\');

        final CsvSchema schema = provider.settings.hasHeader()
            ? baseSchema.withHeader()
            : baseSchema.withColumnsFrom(createHeaderSchema(path));

        try (
            InputStream inputStream = provider.getInputStream();
        ) {
            final MappingIterator<Map<String, String>> reader = new CsvMapper()
                .readerFor(Map.class)
                .with(schema)
                .readValues(inputStream);

            @Nullable Map<String, AccessPath> columns = null;

            while (reader.hasNext()) {
                if (columns == null)
                    columns = createColumns(reader.next(), path);

                forest.addRecord(createRecord(columns, reader.nextValue()));
            }
        }
        catch (IOException e) {
            throw PullForestException.inner(e);
        }

        return forest;
    }

    private CsvSchema createHeaderSchema(ComplexProperty path) {
        // If no header is provided, we have to hope the columns are in the correct order ...
        final var builder = CsvSchema.builder();
        path.subpaths().stream()
            .map(property -> ((StringName) property.name()).value)
            .forEach(builder::addColumn);

        return builder.build();
    }

    private Map<String, AccessPath> createColumns(Map<String, String> row, ComplexProperty path) {
        final Map<String, AccessPath> columns = new TreeMap<>();

        for (final String name : row.keySet()) {
            final @Nullable AccessPath property = path.findSubpathByName(name);

            if (property != null)
                columns.put(name, property);
        }

        return columns;
    }

    private RootRecord createRecord(Map<String, AccessPath> columns, Map<String, String> line) {
        final var rootRecord = new RootRecord();

        for (final var entry : columns.entrySet()) {
            final var name = entry.getKey();
            final var property = entry.getValue();
            final var value = line.get(name);

            if (property.name() instanceof DynamicName) {
                rootRecord.addDynamicRecordWithValue(property, name, value);
                continue;
            }

            rootRecord.addSimpleRecord(property.signature(), value);
        }

        return rootRecord;
    }

    // #region Querying

    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("CsvPullWrapper.executeQuery not implemented.");
    }

    @Override public List<String> getKindNames() {
        return List.of(provider.getKindName());
    }

    @Override public DataResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filter) {
        throw new UnsupportedOperationException("CsvPullWrapper.getRecords not implemented.");
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        throw new UnsupportedOperationException("CsvPullWrapper.getReferences not implemented.");
    }

    @Override public DataResponse getQueryResult(QueryContent query) {
        throw new UnsupportedOperationException("CsvPullWrapper.getQueryResult not implemented.");
    }

    // #endregion

}
