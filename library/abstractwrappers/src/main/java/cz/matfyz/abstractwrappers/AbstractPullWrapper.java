package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.AdminerFilter;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException;

    QueryResult executeQuery(QueryStatement statement);

    /**
     * Retrieves a list of kind names with pagination support.
     *
     * @param limit  the maximum number of records to return.
     * @param offset the starting position of records.
     * @return a {@link KindNameResponse} containing the list of kind names and related metadata.
     */
    KindNameResponse getKindNames(String limit, String offset);

    /**
     * Retrieves data for a specific kind with pagination support and optional filters.
     *
     * @param kindName the name of the kind to retrieve.
     * @param limit    the maximum number of records to return.
     * @param offset   the starting position of records.
     * @param filter   a list of {@link AdminerFilter} objects to apply as filters; can be null if no filters are needed.
     * @return a {@link DataResponse} containing the data for the specified kind and related metadata.
     */
    DataResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filter);

    /**
     * Retrieves a list of foreign key relationships for the specified kind.
     *
     * @param kindName The name of the kind for which to fetch foreign key relationships.
     * @return A {@link List} of {@link Reference} objects representing the foreign key relationships.
     */
    List<Reference> getReferences(String datasourceId, String kindName);

}
