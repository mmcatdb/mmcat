package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException;

    /**
     * Executes an arbitrary query on the data source and returns the result.
     */
    QueryResult executeQuery(QueryStatement statement);

    /**
     * Retrieves all list kind names.
     */
    List<String> getKindNames();

    /**
     * Retrieves data for the kind with pagination support and optional filters.
     *
     * @param kindName - The name of the kind to retrieve.
     * @param limit - The maximum number of records to return.
     * @param offset - The starting position of records.
     * @param filter - A list of {@link AdminerFilter} filters to apply as filters. Can be null if no filters are needed.
     */
    DataResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filter);

    // FIXME Remove the datasourceId parameter. It should be obtained from the wrapper itself.
    /**
     * Retrieves a list of foreign key relationships for the specified kind.
     *
     * @param kindName - The name of the kind for which to fetch foreign key relationships.
     */
    List<Reference> getReferences(String datasourceId, String kindName);

    /**
     * Retrieves the result of the given query.
     *
     * @param query - The custom query.
     */
    DataResponse getQueryResult(QueryContent query);

}
