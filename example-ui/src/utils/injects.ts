import type { Evocat } from "@/types/evocat/Evocat";
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

export const categoryInfoKey: InjectionKey<Ref<SchemaCategoryInfo>> = Symbol('category');

export function useSchemaCategoryInfo(): Ref<SchemaCategoryInfo> {
    const category = inject(categoryInfoKey);
    if (category === undefined)
        throw new Error('Schema category info not injected.');

    return category;
}

export const evocatKey: InjectionKey<Ref<Evocat>> = Symbol('evocat');

export function useEvocat(): Ref<Evocat> {
    const evocat = inject(evocatKey);
    if (evocat === undefined)
        throw new Error('Evocat not injected.');

    return evocat;
}
