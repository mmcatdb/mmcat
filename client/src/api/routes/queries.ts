import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, POST, PUT } from '../routeFunctions';
import type { Id } from '@/types/id';
import type { QueryDescription, QueryFromServer, QueryInit, QueryEdit } from '@/types/query';

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
    describe: POST<Empty, QueryDescription, QueryInput>(
        () => `/queries/describe`,
    ),
    getQueriesInCategory: GET<{ categoryId: StringLike }, QueryFromServer[]>(
        u => `/schema-categories/${u.categoryId}/queries`,
    ),
    getQuery: GET<{ queryId: StringLike }, QueryFromServer>(
        u => `queries/${u.queryId}`,
    ),
    createQuery: POST<Empty, QueryFromServer, QueryInit>(
        () => `/queries`,
    ),
    deleteQuery: DELETE<{ queryId: StringLike }, void>(
        u => `/queries/${u.queryId}`,
    ),
    updateQuery: PUT<{ queryId: StringLike }, QueryFromServer, QueryEdit>(
        u => `/queries/${u.queryId}`,
    ),
};

export default queries;
