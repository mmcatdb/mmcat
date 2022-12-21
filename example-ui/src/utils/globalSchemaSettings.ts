import { inject } from "vue";

export function tryUseSchemaCategory(): number | undefined {
    return inject<number>('schemaCategoryId');
}

export function useSchemaCategory(): number {
    const id = tryUseSchemaCategory();

    if (id === undefined)
        throw new Error('Schema category id not injected.');

    return id;
}
