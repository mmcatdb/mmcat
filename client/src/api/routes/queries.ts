import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, PATCH, POST, PUT } from '../routeFunctions';
import type { Id } from '@/types/id';
import type { QueryDescription, QueryResponse, QueryInit, QueryContentEdit, QueryResult, QueryEdit } from '@/types/query';

export type QueryInput = {
    categoryId: Id;
    /** If defined, the execution should count towards the query's stats. */
    queryId: Id | undefined;
    queryString: string;
};

export const queriesApi = {
    execute: POST<Empty, QueryResult, QueryInput>(
        () => `/queries/execute`,
    ),
    describe: POST<Empty, QueryDescription, QueryInput>(
        () => `/queries/describe`,
    ),
    getQueriesInCategory: GET<{ categoryId: StringLike }, QueryResponse[]>(
        u => `/schema-categories/${u.categoryId}/queries`,
    ),
    getQuery: GET<{ queryId: StringLike }, QueryResponse>(
        u => `queries/${u.queryId}`,
    ),
    createQuery: POST<Empty, QueryResponse, QueryInit>(
        () => `/queries`,
    ),
    deleteQuery: DELETE<{ queryId: StringLike }, void>(
        u => `/queries/${u.queryId}`,
    ),
    updateQuery: PATCH<{ queryId: StringLike }, QueryResponse, QueryEdit>(
        u => `/queries/${u.queryId}`,
    ),
    updateQueryContent: PUT<{ queryId: StringLike }, QueryResponse, QueryContentEdit>(
        u => `/queries/${u.queryId}/content`,
    ),
};
