import { useReducer, type Dispatch } from 'react';
import { FreeSelection } from '../category/graph/selection';
import { getNodeKey } from '../category/graph/categoryGraph';
import { type Category } from '@/types/schema';
import { type GraphMoveEvent } from '../graph/graphEngine';
import { type Adaptation } from './adaptation';
import { categoryToKindGraph, type KindGraph } from './kindGraph';

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
    return {
        category,
        adaptation,
        graph: categoryToKindGraph(category, objex => adaptation.settings.objexes.get(objex.key)?.datasource),
        selection: FreeSelection.create(),
        form: {
            explorationWeight: adaptation.settings.explorationWeight,
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
};

type FormAction = {
    type: 'form';
    explorationWeight: number;
};

function form(state: AdaptationSettingsState, action: FormAction): AdaptationSettingsState {
    return { ...state, form: { ...state.form, explorationWeight: action.explorationWeight } };
}
