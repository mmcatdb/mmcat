import { type PathGraphProvider, PathSelection } from './selection';
import { type Dispatch, useEffect, useRef } from 'react';
import { Signature } from '@/types/identifiers';
import { cn } from '@/components/common/utils';
import { v4 } from 'uuid';
import { getEdgeId, type CategoryNode, getEdgeSignature } from './categoryGraph';
import { Button } from '@heroui/react';

type SignatureInputProps = {
    pathGraphProvider: PathGraphProvider;
    selection: PathSelection | unknown;
    selectionKey: string | undefined;
    dispatch: Dispatch<SignatureInputAction>;
    value: Signature;
    onChange: (value: Signature) => void;
    fromNode: CategoryNode;
    label: string;
    variant?: 'default' | 'compact';
    autofocus?: boolean;
    className?: string;
};

type SignatureInputAction = {
    type: 'selection';
    selection: PathSelection | undefined;
    selectionKey?: string;
};

export function SignatureInput({ pathGraphProvider, selection, selectionKey, dispatch, value, onChange, fromNode, label, variant = 'default', autofocus, className }: SignatureInputProps) {
    // The key is valid through the lifetime of the component.
    const keyRef = useRef(v4());
    const isActive = selectionKey === keyRef.current;

    function dispatchSelection(selection: PathSelection | undefined) {
        dispatch({ type: 'selection', selection, selectionKey: keyRef.current });
    }

    function activate() {
        let selection = PathSelection.create(pathGraphProvider, [ fromNode.id ]);
        if (value)
            selection = appendPathSelection(selection, value);

        dispatchSelection(selection);
    }

    function toggleIsActive() {
        // If the selection is acive, we turn it off. Otherwise, we start a new one.
        if (isActive)
            dispatchSelection(undefined);
        else
            activate();
    }

    useEffect(() => {
        if (autofocus)
            activate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Stop selection on unmount.
    useEffect(() => {
        return () => {
            dispatchSelection(undefined);
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Propagate changes when the selection changes. Not ideal but who cares.
    useEffect(() => {
        if (isActive && selection instanceof PathSelection) {
            const nextValue = getPathSignature(selection);
            if (!nextValue.equals(value))
                onChange(nextValue);
        }
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ selection ]);

    if (variant === 'compact') {
        return (
            <Button
                size='sm'
                className={cn('min-w-8 transition-none', isActive && 'px-[10px] py-[6px] border-2 border-primary-500', className)}
                onPress={toggleIsActive}
                title={label}
            >
                {value.toString()}
            </Button>
        );
    }

    return (
        <button
            className={cn('w-full px-3 py-2 rounded-medium flex flex-col items-start shadow-xs cursor-pointer bg-default-100 hover:bg-default-200',
                isActive && 'px-[10px] py-[6px] border-2 border-primary-500',
                className,
            )}
            onClick={toggleIsActive}
        >
            <label className='text-sm/5 text-default-600 cursor-pointer'>{label}</label>

            <div className='text-small leading-5 text-default-foreground break-words'>
                {value.toString()}
            </div>
        </button>
    );
}

function appendPathSelection(selection: PathSelection, signature: Signature): PathSelection {
    const nodeIds: string[] = [];
    const edgeIds: string[] = [];

    let lastNodeId = selection.lastNodeId;

    for (const base of signature.toBases()) {
        const nonDual = base.isBaseDual ? base.dual() : base;
        const edge = selection.pathGraph.edges.get(getEdgeId(nonDual))!;
        const nextNodeId = edge.from === lastNodeId ? edge.to : edge.from;
        nodeIds.push(nextNodeId);
        edgeIds.push(edge.id);
        lastNodeId = nextNodeId;
    }

    return selection.add(nodeIds, edgeIds);
}

function getPathSignature(selection: PathSelection): Signature {
    const nodeIds = [ ...selection.nodeIds ];

    const signatures = selection.edgeIds.map((edgeId, index) => {
        const edge = selection.pathGraph.edges.get(edgeId)!;
        const fromNodeId = nodeIds[index];
        const signature = getEdgeSignature(edgeId);
        return edge.from === fromNodeId ? signature : signature.dual();
    });

    return Signature.concatenate(...signatures);
}
