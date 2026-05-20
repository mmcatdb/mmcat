import type { Empty, StringLike } from '@/types/api/routes';
import { DELETE, GET, POST } from '../routeFunctions';
import type { CategoryResponse, CategoryInfoResponse, CategoryInit, CategoryStats } from '@/types/schema';
import type { SchemaUpdateResponse, SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

export const schemasApi = {
    getAllCategoryInfos: GET<Empty, CategoryInfoResponse[]>(
        () => `/schema-categories`,
    ),
    createNewCategory: POST<Empty, CategoryInfoResponse, CategoryInit>(
        () => `/schema-categories`,
    ),
    createExampleCategory: POST<{ type: string }, CategoryInfoResponse>(
        u => `/example-schema/${u.type}`,
    ),
    getCategoryInfo: GET<{ id: StringLike }, CategoryInfoResponse>(
        u => `/schema-categories/${u.id}/info`,
    ),
    getCategory: GET<{ id: StringLike }, CategoryResponse>(
        u => `/schema-categories/${u.id}`,
    ),
    getCategoryStats: GET<{ id: StringLike }, CategoryStats>(
        u => `/schema-categories/${u.id}/stats`,
    ),
    updateCategory: POST<{ id: StringLike }, CategoryResponse, SchemaUpdateInit>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    getCategoryUpdates: GET<{ id: StringLike }, SchemaUpdateResponse[]>(
        u => `/schema-categories/${u.id}/updates`,
    ),
    deleteCategory: DELETE<{ id: StringLike }, CategoryResponse>(
        u => `/schema-categories/${u.id}`,
    ),
};
