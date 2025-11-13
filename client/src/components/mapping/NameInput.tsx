import { type MappingEditorState, type MappingEditorDispatch } from './useMappingEditor';
import { Input, Select, SelectItem } from '@heroui/react';
import { type Dispatch, useEffect, useMemo, useState } from 'react';
import { DynamicName, type Name, StringName, TypedName } from '@/types/identifiers';

type NameInputProps = {
    state: MappingEditorState;
    dispatch: MappingEditorDispatch;
    name: Name;
    setName: Dispatch<Name>;
};

export function NameInput({ state, name, setName }: NameInputProps) {
    const [ inner, setInner ] = useState(toInnerName(name));

    useEffect(() => {
        setInner(toInnerName(name));
    }, [ name ]);

    const typeOptions = useMemo(() => state.datasource.specialNames.map(sn => ({ key: sn, label: sn })), [ state.datasource ]);

    function setStringValue(value: string) {
        setInner(value);
        const next = fromInnerName(value, name, state);
        if (next && !next.equals(name))
            setName(next);
    }

    const isStringName = name instanceof StringName;

    // Let's combine string and dynamic names - they are both editable text inputs.
    if (name instanceof StringName || name instanceof DynamicName) {
        return (
            <Input
                label={isStringName ? 'Name' : 'Key pattern'}
                value={inner}
                onChange={e => setStringValue(e.target.value)}
                placeholder={isStringName ? 'Enter property name' : 'Enter key pattern'}
                fullWidth
            />
        );
    }

    // No indexed name - that one can't be edited. So this is just a plain old typed name.
    return (
        <Select
            items={typeOptions}
            label='Name'
            required
            selectedKeys={new Set([ name.type ])}
            onSelectionChange={keys => {
                const value = (keys as Set<string>).values().next().value;
                if (value)
                    setName(new TypedName(value));
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

function toInnerName(name: Name): string {
    if (name instanceof StringName)
        return name.value;
    if (name instanceof DynamicName)
        return name.pattern ?? '';
    return '';
}

function fromInnerName(inner: string, prev: Name, state: MappingEditorState): Name | undefined {
    if (prev instanceof StringName) {
        const validName = state.datasource.createValidPropertyName(inner);
        if (validName === undefined)
            return undefined;

        return validName === prev.value ? prev : new StringName(validName);
    }

    if (prev instanceof DynamicName) {
        const trimmedPattern = inner.trim();
        const pattern = trimmedPattern.length === 0 ? undefined : trimmedPattern;
        if (pattern && !DynamicName.isPatternValid(pattern))
            return undefined;

        return pattern === prev.pattern ? prev : new DynamicName(pattern);
    }
}
