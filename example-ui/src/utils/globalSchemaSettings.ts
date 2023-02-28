import type { Id } from "@/types/id";
import type { SchemaCategoryInfo } from "@/types/schema";
import { inject, type InjectionKey, type Ref } from "vue";

export const categoryIdKey: InjectionKey<Ref<Id>> = Symbol('categoryId');

export function useSchemaCategoryId(): Id {
    const id = inject(categoryIdKey);
    if (id === undefined)
        throw new Error('Schema category id not injected.');

    return id.value;
}

export const categoryKey: InjectionKey<Ref<SchemaCategoryInfo>> = Symbol('category');

export function useSchemaCategory(): SchemaCategoryInfo {
    const category = inject(categoryKey);
    if (category === undefined)
        throw new Error('Schema category not injected.');

    return category.value;
}
