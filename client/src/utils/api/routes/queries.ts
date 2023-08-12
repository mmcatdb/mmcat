import type { Empty } from '@/types/api/routes';
import { POST } from '../routeFunctions';
import type { Id } from '@/types/id';

export type QueryInput = {
    categoryId: Id;
    queryString: string;
};

export type QueryResult = {
    jsonValues: string[];
};

const queries = {
    execute: POST<Empty, QueryResult, QueryInput>(
        () => `/execute`,
    ),
};

export default queries;
