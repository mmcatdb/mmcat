import { type MappingEditorState } from './useMappingEditor';
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react';
import { type AccessPath, AccessPathType, getAccessPathType, transformAccessPathType } from '@/types/mapping';

type PropertyTypeInputProps = {
    state: MappingEditorState;
    property: AccessPath;
    setProperty: (property: AccessPath) => void;
    original: AccessPath;
};

export function PropertyTypeInput({ state, property, setProperty, original }: PropertyTypeInputProps) {
    const type = getAccessPathType(property);

    function setType(value: AccessPathType) {
        setProperty(transformAccessPathType(value, property, original));
    }

    return (
        <Dropdown shouldBlockScroll={false}>
            <DropdownTrigger>
                {/* The width is very precisely chosen to fit all possible label types. */}
                <Button className='h-14 min-w-[72px] px-3 py-2 flex-col items-start justify-start gap-0 shadow-xs scale-100!' disableAnimation>
                    <div className='text-sm/5 text-default-600'>Type</div>
                    <div className='text-small leading-5 text-default-foreground'>{accessPathTypeLabels[type]}</div>
                </Button>
            </DropdownTrigger>

            <DropdownMenu
                disallowEmptySelection
                disableAnimation
                selectionMode='single'
                selectedKeys={new Set([ type ])}
                onSelectionChange={keys => setType((keys as Set<string>).values().next().value! as AccessPathType)}
                disabledKeys={state.datasource.specialNames.length === 0 ? [ AccessPathType.special ] : undefined}
            >
                {Object.entries(accessPathTypeLabels).map(([ key, label ]) => (
                    <DropdownItem key={key}>{label}</DropdownItem>
                ))}
            </DropdownMenu>
        </Dropdown>
    );
}

const accessPathTypeLabels: Record<AccessPathType, string> = {
    [AccessPathType.default]: 'Basic',
    [AccessPathType.map]: 'Map',
    [AccessPathType.array]: 'Array',
    [AccessPathType.special]: 'Special',
};
