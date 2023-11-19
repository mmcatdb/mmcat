import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, POST } from '../routeFunctions';
import type { Id } from '@/types/id';
import type { QueryInit, QueryVersionFromServer, QueryVersionInit, QueryWithVersionFromServer, QueryWithVersionsFromServer } from '@/types/query';

export type QueryInput = {
    categoryId: Id;
    queryString: string;
};

export type QueryResult = {
    rows: string[];
};

const queries = {
    execute: POST<Empty, QueryResult, QueryInput>(
        () => `/queries/execute`,
    ),
    getQueriesInCategory: GET<{ categoryId: StringLike }, QueryWithVersionFromServer[]>(
        u => `/schema-categories/${u.categoryId}/queries`,
    ),
    getQuery: GET<{ queryId: StringLike }, QueryWithVersionFromServer>(
        u => `queries/${u.queryId}`,
    ),
    getQueryWithVersions: GET<{ queryId: StringLike }, QueryWithVersionsFromServer>(
        u => `queries/${u.queryId}/with-versions`,
    ),
    createQuery: POST<Empty, QueryWithVersionFromServer, QueryInit>(
        () => `/queries`,
    ),
    deleteQuery: DELETE<{ queryId: StringLike }, void>(
        u => `/queries/${u.queryId}`,
    ),
    createQueryVersion: POST<{ queryId: StringLike }, QueryVersionFromServer, QueryVersionInit>(
        u => `/queries/${u.queryId}/versions`,
    ),
};

export default queries;
