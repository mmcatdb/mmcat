import { type Dispatch, type KeyboardEvent, type SetStateAction, useCallback, useMemo, useState } from 'react';
import { Button, Input } from '@heroui/react';
import { type CategoryEditorForm, type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { type FormPosition, toFormNumber, toNumber, toPosition } from '@/types/utils/common';
import { PencilIcon, PlusIcon, TrashIcon, XMarkIcon } from '@heroicons/react/20/solid';
import { ObjexIds, Signature, SignatureId } from '@/types/identifiers';
import { SignatureInput } from '../graph/SignatureInput';
import { DefaultPathGraphProvider } from '../graph/selection';
import { type Objex } from '@/types/schema';
import { getNodeId } from '../graph/categoryGraph';
import { cn } from '@/components/utils';

type ObjexEditorProps = {
    state: CategoryEditorState;
    dispatch: CategoryEditorDispatch;
};

export function ObjexEditor({ state, dispatch }: ObjexEditorProps) {
    const key = (state.form as Extract<CategoryEditorForm, { type: 'objex' }>).key;
    const objex = state.evocat.category.getObjex(key);

    const [ label, setLabel ] = useState(objex?.metadata.label ?? '');
    const [ position, setPosition ] = useState<FormPosition>(objex.metadata.position);
    const isIdsEnabled = true; // TODO
    const [ ids, setIds ] = useState<Signature[][]>(() => objex.schema.ids.isEmpty ? [] : objex.schema.ids.signatureIds.map(id => [ ...id.signatures ]));

    const finalLabel = label.trim();
    const idsObject = useMemo(() => ObjexIds.build(ids), [ ids ]);
    const finalIds = isIdsEnabled ? idsObject : objex.schema.ids;

    const isValid = !!finalLabel;
    const isChanged = finalLabel !== objex.metadata.label ||
        position.x !== objex.metadata.position.x ||
        position.y !== objex.metadata.position.y ||
        !finalIds.equals(objex.schema.ids);
    const isDisabled = !isValid || !isChanged;

    function apply() {
        state.evocat.updateObjex(key, {
            label: finalLabel,
            position: toPosition(position),
            ids: finalIds,
        });

        dispatch({ type: 'objex', selectKey: key });
    }

    function handleKeyDown(e: KeyboardEvent) {
        if (e.key === 'Enter' && !isDisabled) {
            e.preventDefault();
            apply();
        }
    }

    return (
        <div className='flex flex-col gap-2' onKeyDown={handleKeyDown}>
            <h3 className='text-lg font-semibold'>Update Object</h3>

            <p className='my-1'>
                <span className='font-bold'>Key:</span> {key.toString()}
            </p>

            <Input
                label='Label'
                value={label}
                onChange={e => setLabel(e.target.value)}
                placeholder='Enter object label'
            />

            <div className='grid grid-cols-2 gap-2'>
                <Input
                    label='Position x'
                    type='number'
                    value={toNumber(position.x).toFixed(0)}
                    onChange={e => setPosition({ ...position, x: toFormNumber(e.target.value) })}
                />

                <Input
                    label='Position y'
                    type='number'
                    value={toNumber(position.y).toFixed(0)}
                    onChange={e => setPosition({ ...position, y: toFormNumber(e.target.value) })}
                />
            </div>

            <div className='flex items-center justify-between'>
                <span className='font-bold'>Ids:</span>

                {!isIdsEnabled && (
                    <span className='italic'>Properties can{'\''}t have ids.</span>
                )}
            </div>

            {isIdsEnabled && (
                <ObjexIdsEditor state={state} dispatch={dispatch} objex={objex} ids={ids} setIds={setIds} />
            )}

            <div className='mt-1 grid grid-cols-2 gap-2'>
                <Button onPress={() => dispatch({ type: 'form', operation: 'cancel' })}>
                    Cancel
                </Button>

                <Button color='primary' onPress={apply} isDisabled={isDisabled}>
                    Apply
                </Button>
            </div>
        </div>
    );
}

type ObjexIdsEditorProps = ObjexEditorProps & {
    objex: Objex;
    ids: Signature[][];
    setIds: Dispatch<SetStateAction<Signature[][]>>;
};

function ObjexIdsEditor({ state, dispatch, objex, ids, setIds }: ObjexIdsEditorProps) {
    const [ selected, setSelected ] = useState<number>();

    function addId() {
        setIds([ ...ids, [ Signature.empty() ] ]);
        setSelected(ids.length);
    }

    function removeId(row: number) {
        setIds(ids.filter((_, r) => r !== row));
        if (selected === row)
            setSelected(undefined);
    }

    const setId = useCallback((nextId: Signature[]) => {
        setIds(prev => prev.map((prevId, r) => r !== selected ? prevId : nextId));
    }, [ selected, setIds ]);

    const buildIds = useMemo(() => ids.map(id => SignatureId.create(id)), [ ids ]);

    return (
        <div className='flex flex-col gap-2'>
            {ids.map((id, index) => (index === selected) ? (
                <SignatureIdEditor
                    key={index}
                    state={state}
                    dispatch={dispatch}
                    objex={objex}
                    id={ids[selected]}
                    setId={setId}
                    isValid={isIdValid(buildIds, index)}
                />
            ) : (
                <div key={index} className='flex items-center gap-1 rounded-small bg-default-100'>
                    <div className={cn('min-w-0 grow px-2 py-1 break-words', !isIdValid(buildIds, index) && 'text-warning-500')}>
                        {'(' + id.map(s => s.toString()).join(', ') + ')'}
                    </div>

                    <Button size='sm' isIconOnly variant='light' onPress={() => setSelected(index)} isDisabled={selected === index}>
                        <PencilIcon className='size-4' />
                    </Button>
                    <Button size='sm' isIconOnly variant='light' color='danger' onPress={() => removeId(index)}>
                        <TrashIcon className='size-4' />
                    </Button>
                </div>
            ))}

            <Button size='sm' onPress={addId}>
                <PlusIcon className='size-5' /> New id
            </Button>
        </div>
    );
}

function isIdValid(ids: SignatureId[], index: number): boolean {
    const targetId = ids[index];

    for (let i = 0; i < index; i++) {
        if (ids[i].equals(targetId))
            return false;
    }

    if (ids.length > 1 && targetId.isEmpty)
        return false;

    return true;
}

type SignatureIdEditorProps = ObjexEditorProps & {
    objex: Objex;
    id: Signature[];
    setId: Dispatch<Signature[]>;
    isValid: boolean;
};

function SignatureIdEditor({ state, dispatch, objex, id, setId, isValid }: SignatureIdEditorProps) {
    function setSiganture(index: number, signature: Signature) {
        setId(id.map((s, i) => i !== index ? s : signature));
    }

    function removeSignature(index: number) {
        setId(id.filter((_, i) => i !== index));
    }

    const [ shouldAutofocus, setShouldAutofocus ] = useState(false);

    function addSignature() {
        setId([ ...id, Signature.empty() ]);
        // We want to autofocus all newly added signatures.
        setShouldAutofocus(true);
    }

    const fromNode = state.graph.nodes.get(getNodeId(objex.schema.key))!;

    return (
        <div className='flex flex-wrap items-center gap-1 rounded-small'>
            {id.map((signature, index) => (
                <div key={index} className='flex items-center rounded-small bg-default-100'>
                    <SignatureInput
                        selection={state.selection}
                        selectionKey={state.selectionKey}
                        // FIXME use a correct filter for ids
                        pathGraphProvider={new DefaultPathGraphProvider(state.evocat.category)}
                        dispatch={dispatch}
                        value={signature}
                        onChange={value => setSiganture(index, value)}
                        fromNode={fromNode}
                        label={`Signature at index ${index}`}
                        variant='compact'
                        autofocus={shouldAutofocus}
                        className={(!isValid || !isSignatureValid(id, index)) ? 'text-warning-500' : undefined}
                    />

                    <Button size='sm' isIconOnly variant='light' color='danger' onPress={() => removeSignature(index)}>
                        <XMarkIcon className='size-4' />
                    </Button>
                </div>
            ))}

            {id.length === 0 && (
                <span className='italic px-2 py-1'>empty</span>
            )}

            <div className='grow flex justify-end'>
                <Button size='sm' isIconOnly onPress={addSignature}>
                    <PlusIcon className='size-5' />
                </Button>
            </div>
        </div>
    );
}

function isSignatureValid(signatures: Signature[], index: number): boolean {
    const targetSignature = signatures[index];

    for (let i = 0; i < index; i++) {
        if (signatures[i].equals(targetSignature))
            return false;
    }

    if (signatures.length > 1 && targetSignature.isEmpty)
        return false;

    return true;
}
