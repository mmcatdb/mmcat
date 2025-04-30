package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.KindNamesResponse;
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

import cz.matfyz.core.mapping.StaticName;

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

    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    /**
     * Pulls a forest of records from a CSV file based on a complex property path and query content.
     */
    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        final var forest = new ForestOfRecords();

        final CsvSchema baseSchema = CsvSchema.emptySchema()
            .withColumnSeparator(provider.getSeparator())
            .withEscapeChar('\\');

        final CsvSchema schema = provider.hasHeader()
            ? baseSchema.withHeader()
            : baseSchema.withColumnsFrom(createHeaderSchema(path));

        replacedNames = path.copyWithoutDynamicNames().replacedNames();

        try (
            InputStream inputStream = provider.getInputStream();
        ) {
            final MappingIterator<Map<String, String>> reader = new CsvMapper()
                .readerFor(Map.class)
                .with(schema)
                .readValues(inputStream);

            @Nullable Map<String, SimpleProperty> columns = null;

            while (reader.hasNext()) {
                if (columns == null)
                    columns = createColumns(reader.next(), path);

                forest.addRecord(createRecord(columns, reader.nextValue()));
            }
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

    private Map<String, SimpleProperty> createColumns(Map<String, String> row, ComplexProperty path) {
        final Map<String, SimpleProperty> columns = new TreeMap<>();

        for (final String name : row.keySet()) {
            final @Nullable AccessPath property = path.findSubpathByName(name);

            if (property != null)
                columns.put(name, (SimpleProperty) property);
        }

        return columns;
    }

    private RootRecord createRecord(Map<String, SimpleProperty> columns, Map<String, String> line) {
        final var rootRecord = new RootRecord();

        for (final var entry : columns.entrySet()) {
            final var name = entry.getKey();
            final var property = entry.getValue();
            final var value = line.get(name);

            if (!(property.name() instanceof final DynamicName dynamicName)) {
                rootRecord.addSimpleRecord(property.signature(), value);
                continue;
            }

            final var replacement = replacedNames.get(dynamicName);
            final var replacer = rootRecord.addDynamicReplacer(replacement.prefix(), replacement.name(), name);
            replacer.addSimpleRecord(replacement.value().signature(), value);
        }

        return rootRecord;
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

    @Override public KindNamesResponse getKindNames(String limit, String offset) {
        throw new UnsupportedOperationException("CsvPullWrapper.getKindNames not implemented.");
    }

    @Override public DataResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filter) {
        throw new UnsupportedOperationException("CsvPullWrapper.getKind not implemented.");
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        throw new UnsupportedOperationException("CsvPullWrapper.getReferences not implemented.");
    }

    @Override public DataResponse getQueryResult(String query){
        throw new UnsupportedOperationException("CsvPullWrapper.getQueryResult not implemented.");
    }

}
