import { type MappingEditorState, type MappingEditorDispatch } from './useMappingEditor';
import { PathSelection } from '@/components/graph/graphSelection';
import { getPathSignature } from '@/components/graph/graphUtils';
import { useEffect, useRef } from 'react';
import { type Key, type Signature } from '@/types/identifiers';
import { cn } from '../utils';
import { v4 } from 'uuid';
import { collectAccessPathSignature } from '@/types/mapping';
import { type Category } from '@/types/schema';

type SignatureInputProps = {
    value: Signature;
    onChange: (value: Signature) => void;
    label: string;
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
    isFromParent?: boolean;
};

export function SignatureInput({ value, onChange, label, state, dispatch, isFromParent }: SignatureInputProps) {
    // The key is valid through the lifetime of the component.
    const keyRef = useRef(v4());
    const isActive = state.selectionKey === keyRef.current;

    function toggleIsActive() {
        // If the selection is acive, we turn it off. Otherwise, we start a new one.
        const selection = isActive ? undefined : createPathSelection(state, !!isFromParent, value);
        dispatch({ type: 'accessPath', operation: 'selection', selectionKey: keyRef.current, selection });
    }

    // Stop selection on unmount.
    useEffect(() => {
        const selectionKey = keyRef.current;
        return () => {
            dispatch({ type: 'accessPath', operation: 'selection', selectionKey, selection: undefined });
        };
    }, [ dispatch ]);

    // Propagate changes when the selection changes. Not ideal but who cares.
    useEffect(() => {
        if (isActive && state.selection instanceof PathSelection)
            onChange(getPathSignature(state.graph, state.selection));
    }, [ state.selection ]);

    return (
        <div
            className={cn('w-full px-3 py-2 rounded-medium flex flex-col shadow-xs cursor-pointer bg-default-100 hover:bg-default-200',
                isActive && 'px-[10px] py-[6px] border-2 border-primary-500',
            )}
            onClick={toggleIsActive}
        >
            <label className='text-sm/5 text-default-600 cursor-pointer'>{label}</label>

            <div className='text-small leading-5 text-default-foreground break-words'>
                {value.toString()}
            </div>
        </div>
    );
}

/**
 * Creates selection starting in the currently selected objex. If no property is selected or the path is impossible, an error is thrown.
 * @param isFromParent - If true, the selection will start in the parent of the currently selected objex.
 * @param preselected - If defined, the selection will have preselected this signature.
 */
function createPathSelection(state: MappingEditorState, isFromParent: boolean, preselected?: Signature) {
    if (!state.selectedPropertyPath)
        throw new Error('Can\'t create path selection without selected property.');

    const toPath = isFromParent ? state.selectedPropertyPath.pop() : state.selectedPropertyPath;
    const pathFromRoot = collectAccessPathSignature(state.form.accessPath, toPath);
    const lastBase = pathFromRoot.tryGetLastBase();

    let toObjexKey: Key;
    if (!lastBase) {
        // If the path doesn't have at least one base signature, it starts from the root objex.
        toObjexKey = state.form.rootObjexKey!;
    }
    else {
        // Otherwise, we can find its morphism and therefore the target objex.
        const edge = state.category.getEdge(lastBase.last);
        toObjexKey = edge.direction ? edge.morphism.schema.codKey : edge.morphism.schema.domKey;
    }

    const parentSelection = PathSelection.create([ toObjexKey.toString() ]);
    return preselected ? appendPathSelection(parentSelection, preselected, state.category) : parentSelection;
}

function appendPathSelection(selection: PathSelection, signature: Signature, category: Category): PathSelection {
    const nodeIds: string[] = [];
    const edgeIds: string[] = [];

    for (const base of signature.toBases()) {
        const edge = category.getEdge(base);
        const toObjexKey = edge.direction ? edge.morphism.schema.codKey : edge.morphism.schema.domKey;
        nodeIds.push(toObjexKey.toString());
        edgeIds.push(edge.morphism.signature.toString());
    }

    return selection.add(nodeIds, edgeIds);
}
