import type { Id } from "@/types/id";
import { inject, type InjectionKey, type Ref } from "vue";

export const categoryIdKey: InjectionKey<Ref<Id>> = Symbol('categoryId');

export function useSchemaCategory(): Id {
    const id = inject(categoryIdKey);
    if (id === undefined)
        throw new Error('Schema category id not injected.');

    return id.value;
}
