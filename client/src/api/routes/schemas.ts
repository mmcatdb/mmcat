import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, POST } from '../routeFunctions';
import type { SchemaCategoryFromServer, SchemaCategoryInfoFromServer, SchemaCategoryInit, SchemaCategoryStats } from '@/types/schema';
import type { SchemaUpdateFromServer, SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

const schemas = {
    getAllCategoryInfos: GET<Empty, SchemaCategoryInfoFromServer[]>(
        () => `/schema-categories`,
    ),
    createNewCategory: POST<Empty, SchemaCategoryInfoFromServer, SchemaCategoryInit>(
        () => `/schema-categories`,
    ),
    createExampleCategory: POST<{ name: string }, SchemaCategoryInfoFromServer>(
        u => `/example-schema/${u.name}`,
    ),
    getCategoryInfo: GET<{ id: StringLike }, SchemaCategoryInfoFromServer>(
        u => `/schema-categories/${u.id}/info`,
    ),
    getCategory: GET<{ id: StringLike }, SchemaCategoryFromServer>(
        u => `/schema-categories/${u.id}`,
    ),
    getCategoryStats: GET<{ id: StringLike }, SchemaCategoryStats>(
        u => `/schema-categories/${u.id}/stats`,
    ),
    updateCategory: POST<{ id: StringLike }, SchemaCategoryFromServer, SchemaUpdateInit>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    getCategoryUpdates: GET<{ id: StringLike }, SchemaUpdateFromServer[]>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    deleteCategory: DELETE<{ id: StringLike }, SchemaCategoryFromServer>(
        u => `/schema-categories/${u.id}`,
    ),
};

export default schemas;
