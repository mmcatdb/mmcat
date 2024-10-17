import type { Graph } from '@/types/categoryGraph';
import type { Evocat } from '@/types/evocat/Evocat';
import type { Id } from '@/types/id';
import type { SchemaCategoryInfo } from '@/types/schema';
import type { Workflow } from '@/types/workflow';
import { inject, type InjectionKey, type Ref, type ShallowRef } from 'vue';
import { useRoute } from 'vue-router';

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

export type EvocatContext = {
    evocat: ShallowRef<Evocat>;
    graph: ShallowRef<Graph>;
};

export const evocatKey: InjectionKey<EvocatContext> = Symbol('evocat');

export function useEvocat(): EvocatContext {
    const evocatContext = inject(evocatKey);
    if (!evocatContext)
        throw new Error('Evocat not injected.');

    return evocatContext;
}

export const workflowKey: InjectionKey<Ref<Workflow | undefined>> = Symbol('workflow');

export function useWorkflow(): Ref<Workflow | undefined> {
    const workflow = inject(workflowKey);
    if (workflow === undefined)
        throw new Error('Workflow not injected.');

    return workflow;
}

export function useWorkflowId(): Id | undefined{
    const route = useRoute();
    return (route.query.workflowId ?? undefined) as Id | undefined;
}
