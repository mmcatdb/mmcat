import { type Position } from '../graph/graphUtils';
import { categoryToGraph } from './categoryGraph';
import { type EditCategoryState } from './editCategoryReducer';

export enum EditorPhase {
    default = 'default',
    createObject = 'createObject',
}

type PhaseState<TPhase extends EditorPhase, TState> = TState & {
    phase: TPhase;
};

export type PhasedState<TState extends PhasedEditorState> = Omit<EditCategoryState, 'editor'> & {
    editor: TState;
};

type PhaseAction<TPhase extends EditorPhase, TAction> = TAction & {
    type: TPhase;
};

export type PhasedEditorState = DefaultState | CreateObjectState;

export function createInitialEditorState(): DefaultState {
    return {
        phase: EditorPhase.default,
    };
}

export type PhasedEditorAction = PhaseAction<EditorPhase.default, DefaultAction> | PhaseAction<EditorPhase.createObject, CreateObjectAction>;

export function phasedEditorReducer(state: EditCategoryState, action: PhasedEditorAction): EditCategoryState {
    if (state.editor.phase !== action.type)
        throw new Error('Invalid state.');

    switch (action.type) {
    case EditorPhase.default: return defaultPhase(state as PhasedState<DefaultState>, action);
    case EditorPhase.createObject: return createObject(state as PhasedState<CreateObjectState>, action);
    }
}

// Default

export type DefaultState = PhaseState<EditorPhase.default, {}>;

export type DefaultAction = {
    operation: 'createObject';
};

function defaultPhase(state: PhasedState<DefaultState>, action: DefaultAction): EditCategoryState {
    if (state.editor.phase !== EditorPhase.default)
        throw new Error('Invalid state.');

    switch (action.operation) {
    case 'createObject': return { ...state, editor: createInitialCreateObjectState() };
    }
}

// Create object

export type CreateObjectState = PhaseState<EditorPhase.createObject, {
    label: string;
    position: Position;
}>;

function createInitialCreateObjectState(): CreateObjectState {
    return {
        phase: EditorPhase.createObject,
        label: '',
        position: { x: 0, y: 0 },
    };
}

export type CreateObjectAction = {
    operation: 'label';
    value: string;
} | {
    operation: 'finish' | 'cancel';
};

function createObject(state: PhasedState<CreateObjectState>, action: CreateObjectAction): EditCategoryState {
    switch (action.operation) {
    case 'label': return { ...state, editor: { ...state.editor, label: action.value } };
    case 'cancel': return { ...state, editor: createInitialEditorState() };
    case 'finish': return { ...state, editor: createInitialEditorState(),
        graph: categoryToGraph(state.evocat.current),
    };
    }
}
