import type { Id } from "@/types/id";
import { inject } from "vue";

export function tryUseSchemaCategory(): Id | undefined {
    return inject<Id>('schemaCategoryId');
}

export function useSchemaCategory(): Id {
    const id = tryUseSchemaCategory();

    if (id === undefined)
        throw new Error('Schema category id not injected.');

    return id;
}
