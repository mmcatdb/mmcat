import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, POST } from '../routeFunctions';
import type { SchemaCategoryResponse, SchemaCategoryInfoResponse, SchemaCategoryInit, SchemaCategoryStats } from '@/types/schema';
import type { SchemaUpdateResponse, SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

export const schemasApi = {
    getAllCategoryInfos: GET<Empty, SchemaCategoryInfoResponse[]>(
        () => `/schema-categories`,
    ),
    createNewCategory: POST<Empty, SchemaCategoryInfoResponse, SchemaCategoryInit>(
        () => `/schema-categories`,
    ),
    createExampleCategory: POST<{ type: string }, SchemaCategoryInfoResponse>(
        u => `/example-schema/${u.type}`,
    ),
    getCategoryInfo: GET<{ id: StringLike }, SchemaCategoryInfoResponse>(
        u => `/schema-categories/${u.id}/info`,
    ),
    getCategory: GET<{ id: StringLike }, SchemaCategoryResponse>(
        u => `/schema-categories/${u.id}`,
    ),
    getCategoryStats: GET<{ id: StringLike }, SchemaCategoryStats>(
        u => `/schema-categories/${u.id}/stats`,
    ),
    updateCategory: POST<{ id: StringLike }, SchemaCategoryResponse, SchemaUpdateInit>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    getCategoryUpdates: GET<{ id: StringLike }, SchemaUpdateResponse[]>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    deleteCategory: DELETE<{ id: StringLike }, SchemaCategoryResponse>(
        u => `/schema-categories/${u.id}`,
    ),
};
