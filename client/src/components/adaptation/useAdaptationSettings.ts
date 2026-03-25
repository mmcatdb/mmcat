import { useReducer, type Dispatch } from 'react';
import { FreeSelection } from '../category/graph/selection';
import { getEdgeId, getNodeKey } from '../category/graph/categoryGraph';
import { type Objex, type Category } from '@/types/schema';
import { type GraphMoveEvent } from '../graph/graphEngine';
import { type Adaptation } from './adaptation';
import { categoryToKindGraph, type KindGraph } from './kindGraph';
import { type Id } from '@/types/id';

export function useAdaptationSettings(category: Category, adaptation: Adaptation) {
    const [ state, dispatch ] = useReducer(adaptationSettingsReducer, { category, adaptation }, createInitialState);

    return { state, dispatch };
}

export type AdaptationSettingsState = {
    category: Category;
    adaptation: Adaptation;
    graph: KindGraph;
    selection: FreeSelection;
    form: AdaptationSettingsForm;
};

function createInitialState({ category, adaptation }: { category: Category, adaptation: Adaptation }): AdaptationSettingsState {
    const morphisms = new Map<string, EdgeForm>();
    category.morphisms.values()
        .filter(m => {
        // Only include edges where both dom and cod do participate in the adaptation.
            const dom = adaptation.settings.objexes.get(m.schema.domKey);
            const cod = adaptation.settings.objexes.get(m.schema.codKey);
            return dom?.mappings.length && cod?.mappings.length;
        })
        .forEach(m => {
            const morphism = adaptation.settings.morphisms.get(m.signature);
            if (!morphism)
            // TODO Not sure if this can happen ...
                return;

            morphisms.set(getEdgeId(m), {
                isReferenceAllowed: morphism.isReferenceAllowed,
                isEmbeddingAllowed: morphism.isEmbeddingAllowed,
            });
        });

    const datasorceGetter = (objex: Objex) => adaptation.settings.objexes.get(objex.key)?.mappings?.map(m => m.datasource) ?? [];

    return {
        category,
        adaptation,
        graph: categoryToKindGraph(category, datasorceGetter),
        selection: FreeSelection.create(),
        form: {
            explorationWeight: adaptation.settings.explorationWeight,
            datasourceIds: new Set(adaptation.settings.datasources.map(ds => ds.id)),
            morphisms,
        },
    };
}

export type AdaptationSettingsDispatch = Dispatch<AdaptationSettingsAction>;

type AdaptationSettingsAction =
    | GraphMoveEvent
    | SelectionAction
    | FormAction;

function adaptationSettingsReducer(state: AdaptationSettingsState, action: AdaptationSettingsAction): AdaptationSettingsState {
    switch (action.type) {
    case 'move': return move(state, action);
    case 'selection': return selection(state, action);
    case 'form': return form(state, action);
    }
}

function move(state: AdaptationSettingsState, event: GraphMoveEvent): AdaptationSettingsState {
    const key = getNodeKey(event.nodeId);
    // TODO Probably need to update the adaptation and then the graph ...
    // state.evocat.updateObjex(key, { position: event.position });

    // Rebuild graph to reflect position changes
    return {
        ...state,
        // graph: categoryToKindGraph(state.category, [ ...state.adaptation.settings.objexes.values() ]),
    };
}

type SelectionAction = {
    type: 'selection';
    selection: FreeSelection;
};

function selection(state: AdaptationSettingsState, { selection }: SelectionAction): AdaptationSettingsState {
    // Restrict selection max one node or one edge.
    if (selection.nodeIds.size > 1 || (selection.nodeIds.size === 1 && selection.edgeIds.size > 0))
        selection = FreeSelection.create([ selection.firstNodeId! ]);
    else if (selection.edgeIds.size > 1)
        selection = FreeSelection.create([], [ selection.firstEdgeId! ]);

    return { ...state, selection };
}

export type AdaptationSettingsForm = {
    explorationWeight: number;
    datasourceIds: Set<Id>;
    /** Morphism that can participate in the adaptation (both dom and cod do participate). */
    morphisms: Map<string, EdgeForm>;
};

type EdgeForm = {
    isReferenceAllowed: boolean;
    isEmbeddingAllowed: boolean;
};

type FormAction = {
    type: 'form';
} & ({
    field: 'explorationWeight';
    value: number;
} | {
    field: 'datasourceIds';
    value: Set<Id>;
} | {
    field: 'morphism';
    edgeId: string;
    edit: Partial<EdgeForm>;
} | {
    // Set entire edges map.
    field: 'morphisms';
    edit: Partial<EdgeForm>;
});

function form(state: AdaptationSettingsState, action: FormAction): AdaptationSettingsState {
    if (action.field === 'morphism') {
        const morphisms = new Map(state.form.morphisms);
        const prev = morphisms.get(action.edgeId)!;
        morphisms.set(action.edgeId, { ...prev, ...action.edit });
        return { ...state, form: { ...state.form, morphisms } };
    }

    if (action.field === 'morphisms') {
        const morphisms = new Map<string, EdgeForm>();
        state.form.morphisms.forEach((prev, key) => morphisms.set(key, { ...prev, ...action.edit }));
        return { ...state, form: { ...state.form, morphisms } };
    }

    return { ...state, form: { ...state.form, [action.field]: action.value } };
}
