import { type MappingEditorState, type MappingEditorDispatch } from './useMappingEditor';
import { type AccessPath } from '@/types/mapping';
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger, Input, Select, SelectItem } from '@heroui/react';
import { type Dispatch, useMemo } from 'react';
import { DynamicName, type Name, Signature, StringName, TypedName } from '@/types/identifiers';
import { type Datasource } from '@/types/Datasource';
import { capitalize } from '@/types/utils/common';
import { SignatureInput } from './SignatureInput';

type NameInputProps = {
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
    name: EditableName;
    setName: Dispatch<EditableName>;
    selected: AccessPath;
};

export function NameInput({ state, dispatch, name, setName, selected }: NameInputProps) {
    function setNameType(type: NameType) {
        const next = switchEditableNameType(type, name, toEditableName(selected.name), state.datasource);
        if (next)
            setName(next);
    }

    return (
        <div className='flex gap-2'>
            <div className='flex flex-col gap-2'>
                <NameInputFields state={state} dispatch={dispatch} name={name} setName={setName} />
            </div>

            <Dropdown shouldBlockScroll={false}>
                <DropdownTrigger>
                    <Button className='h-14 min-w-fit px-3 py-2 flex-col items-start justify-start gap-0 shadow-xs scale-100!' disableAnimation>
                        <div className='text-sm/5 text-default-600'>Name type</div>
                        <div className='text-small leading-5 text-default-foreground'>{nameTypeLabels[name.type]}</div>
                    </Button>
                </DropdownTrigger>

                <DropdownMenu
                    disallowEmptySelection
                    disableAnimation
                    selectionMode='single'
                    selectedKeys={new Set([ name.type ])}
                    onSelectionChange={keys => setNameType((keys as Set<string>).values().next().value! as NameType)}
                    disabledKeys={state.datasource.specialNames.length === 0 ? [ NameType.typed ] : undefined}
                >
                    {Object.entries(nameTypeLabels).map(([ key, label ]) => (
                        <DropdownItem key={key}>{label}</DropdownItem>
                    ))}
                </DropdownMenu>
            </Dropdown>
        </div>
    );
}

export enum NameType {
    string = 'static',
    dynamicKey = 'dynmicKey',
    dynamicIndex = 'dynamicIndex',
    typed = 'typed',
}

const nameTypeLabels: Record<NameType, string> = {
    [NameType.string]: 'String',
    [NameType.dynamicKey]: 'Key',
    [NameType.dynamicIndex]: 'Index',
    [NameType.typed]: 'Special',
};

type NameInputFieldsProps = {
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
    name: EditableName;
    setName: Dispatch<EditableName>;
};

function NameInputFields({ state, dispatch, name, setName }: NameInputFieldsProps) {
    const typeOptions = useMemo(() => state.datasource.specialNames.map(sn => ({ key: sn, label: sn })), [ state.datasource ]);

    if (name.type === NameType.string) {
        return (
            <Input
                label='Property name'
                value={name.value}
                onChange={e => setName({ ...name, value: e.target.value })}
                placeholder='Enter property name'
                fullWidth
            />
        );
    }

    if (name.type === NameType.typed) {
        return (
            <Select
                items={typeOptions}
                label='Property name'
                required
                selectedKeys={new Set([ name.value ])}
                onSelectionChange={keys => {
                    const value = (keys as Set<string>).values().next().value;
                    if (value)
                        setName({ ...name, value });
                }}
            >
                {item => (
                    <SelectItem key={item.key}>
                        {item.label}
                    </SelectItem>
                )}
            </Select>
        );
    }

    const keyOrIndex = name.type === NameType.dynamicKey ? 'key' : 'index';

    return (<>
        <SignatureInput
            value={name.signature}
            onChange={signature => setName({ ...name, signature })}
            label={`Path to ${keyOrIndex}`}
            state={state}
            dispatch={dispatch}
        />

        <Input
            label={`${capitalize(keyOrIndex)} pattern`}
            value={name.pattern}
            onChange={e => setName({ ...name, pattern: e.target.value })}
            placeholder={`Enter ${keyOrIndex} pattern`}
            fullWidth
        />
    </>);
}

type EditableName = {
    type: NameType.string;
    value: string;
} | {
    type: NameType.typed;
    value: string;
} | {
    type: NameType.dynamicKey | NameType.dynamicIndex;
    signature: Signature;
    pattern: string;
};

export function toEditableName(name: Name): EditableName {
    if (name instanceof StringName)
        return { type: NameType.string, value: name.value };

    if (name instanceof DynamicName) {
        const type = name.type === TypedName.KEY ? NameType.dynamicKey : NameType.dynamicIndex;
        return { type, signature: name.signature, pattern: name.pattern ?? '' };
    }

    return { type: NameType.typed, value: name.type };
}

export function fromEditableName(editable: EditableName, state: MappingEditorState): Name | undefined {
    if (editable.type === NameType.string) {
        const validName = state.datasource.createValidPropertyName(editable.value);
        return validName ? new StringName(validName) : undefined;
    }

    if (editable.type === NameType.typed)
        return new TypedName(editable.value);

    const type = editable.type === NameType.dynamicKey ? TypedName.KEY : TypedName.INDEX;
    const trimmedPattern = editable.pattern.trim();
    const pattern = trimmedPattern.length === 0 ? undefined : trimmedPattern;
    if (pattern && !DynamicName.isPatternValid(pattern))
        return undefined;

    return new DynamicName(type, editable.signature, pattern);
}

function switchEditableNameType(type: NameType, prev: EditableName, original: EditableName, datasource: Datasource): EditableName | undefined {
    if (type === prev.type)
        return;

    // If the name is dynamic, we try to just switch types while keeping the other values.
    const isNextDynamic = type === NameType.dynamicKey || type === NameType.dynamicIndex;
    if (isNextDynamic) {
        if (prev.type === NameType.dynamicKey || prev.type === NameType.dynamicIndex)
            return { ...prev, type };
    }
    // This holds also for the original name.
    if (isNextDynamic) {
        if (original.type === NameType.dynamicKey || original.type === NameType.dynamicIndex)
            return { ...original, type };
    }

    // If the types are the same, we just reuse the original name.
    if (type === original.type)
        return original;

    // Fallback to default values.
    if (type === NameType.string)
        return { type, value: '' };
    if (isNextDynamic)
        return { type, signature: Signature.empty(), pattern: '' };
    if (datasource.specialNames.length > 0)
        return { type, value: datasource.specialNames[0] };
}
