import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { SchemaCategoryFromServer, SchemaCategoryInfoFromServer, SchemaCategoryInit } from '@/types/schema';
import type { SchemaUpdateFromServer, SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

const schemas = {
    getAllCategoryInfos: GET<Empty, SchemaCategoryInfoFromServer[]>(
        () => `/schema-categories`,
    ),
    createNewCategory: POST<Empty, SchemaCategoryInfoFromServer, SchemaCategoryInit>(
        () => `/schema-categories`,
    ),
    getCategoryInfo: GET<{ id: StringLike }, SchemaCategoryInfoFromServer>(
        u => `/schema-categories/${u.id}/info`,
    ),
    getCategoryWrapper: GET<{ id: StringLike }, SchemaCategoryFromServer>(
        u => `/schema-categories/${u.id}`,
    ),
    updateCategoryWrapper: POST<{ id: StringLike }, SchemaCategoryFromServer, SchemaUpdateInit>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    getCategoryUpdates: GET<{ id: StringLike }, SchemaUpdateFromServer[]>(
        u => `/schema-categories/${u.id}/updates`,
    ),
};

export default schemas;
