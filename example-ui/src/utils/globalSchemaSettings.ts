import { inject } from "vue";

export function useSchemaCategory(): number {
    const id = inject<number>('schemaCategoryId');

    if (id === undefined)
        throw new Error('Schema category id not injected.');

    return id;
}
