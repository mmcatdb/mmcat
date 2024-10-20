import type { Graph } from '@/types/categoryGraph';
import type { Evocat } from '@/types/evocat/Evocat';
import type { Id } from '@/types/id';
import type { SchemaCategoryInfo } from '@/types/schema';
import type { Workflow } from '@/types/workflow';
import { inject, type InjectionKey, type Ref, type ShallowRef } from 'vue';
import { useRoute } from 'vue-router';

export const categoryInfoKey: InjectionKey<Ref<SchemaCategoryInfo>> = Symbol('category');

export function useSchemaCategoryInfo(): Ref<SchemaCategoryInfo> {
    const category = inject(categoryInfoKey);
    if (category === undefined)
        throw new Error('Schema category info not injected.');

    return category;
}

export function useSchemaCategoryId(): Id {
    return useSchemaCategoryInfo().value.id;
}

export function tryUseSchemaCategoryId(): Id | undefined {
    return inject(categoryInfoKey)?.value.id;
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

export const workflowKey: InjectionKey<Ref<Workflow>> = Symbol('workflow');

export function useWorkflow(): Ref<Workflow> {
    const workflow = inject(workflowKey);
    if (workflow === undefined)
        throw new Error('Workflow not injected.');

    return workflow;
}

export function tryUseWorkflow(): Ref<Workflow> | undefined {
    return inject(workflowKey);
}
