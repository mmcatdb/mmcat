import { useCallback } from 'react';
import { type NamePath, NamePathBuilder } from '@/types/identifiers';
import { AccessPathType, getAccessPathType, type AccessPath, type ParentProperty, type RootProperty, type SimpleProperty } from '@/types/mapping';
import { cn } from '@/components/common/utils';

/** Either an access path or editable access path. */
type Property = ParentProperty | SimpleProperty | AccessPath;

type AccessPathDisplayProps = {
    property: AccessPath | RootProperty;
    selected?: Property;
    onClick?: (path: NamePath) => void;
    className?: string;
};

export function AccessPathDisplay({ property, selected, onClick, className }: AccessPathDisplayProps) {
    const innerOnClick = useCallback((builder: NamePathBuilder) => {
        // The first element is going to be the root property, so we shift it off.
        // We don't take kindly to your types in here!
        onClick?.(builder.shift().build());
    }, [ onClick ]);

    return (
        <div className={cn(outerClassName, className)}>
            <PropertyDisplay
                property={property}
                selected={selected}
                onClick={onClick ? innerOnClick : undefined}
            />
        </div>
    );
}

const outerClassName = 'p-3 rounded-lg bg-default-100 leading-5';

type AccessPathPreviewProps = {
    property: AccessPath;
    className?: string;
};

export function AccessPathPreview({ property, className }: AccessPathPreviewProps) {
    const type = getAccessPathType(property);
    const isComposed = type === AccessPathType.map || type === AccessPathType.array;

    return (
        <div className={cn(outerClassName, className)}>
            <PropertyDisplay property={property} selected={property} collapsedLevel={isComposed ? 1 : 0} />
        </div>
    );
}

type PropertyDisplayProps = {
    property: Property;
    selected?: Property;
    onClick?: ((builder: NamePathBuilder) => void);
    collapsedLevel?: number;
};

function PropertyDisplay({ property, selected, onClick, collapsedLevel }: PropertyDisplayProps) {
    function select() {
        onClick?.(new NamePathBuilder(property.name));
    }

    const innerOnClick = useCallback((builder: NamePathBuilder) => {
        builder.prepend(property.name);
        onClick?.(builder);
    }, [ onClick, property.name ]);

    const isSelected = property === selected;
    const selectorClass = isSelected
        ? 'mm-access-path-selected'
        : onClick
            ? 'mm-access-path-selectable'
            : undefined;

    const isEmpty = !('subpaths' in property) || property.subpaths.length === 0;

    if (isEmpty || collapsedLevel === 0) {
        return (
            <div className={selectorClass}>
                <div className='mm-target w-fit px-1 space-x-2' onClick={select}>
                    <span>{property.name.toString()}:</span>
                    <span>{property.signature.toString()}</span>
                    {!isEmpty && <span>{'{ ... }'}</span>}
                </div>
            </div>
        );
    }

    return (
        <div className={cn('grid grid-cols-[auto_minmax(0,1fr)] gap-y-1 gap-x-3', selectorClass)}>
            <div className='mm-target col-span-2 w-fit px-1 space-x-2' onClick={select}>
                <span>{property.name.toString()}:</span>
                <span>{property.signature.toString()}</span>
                <span>{'{'}</span>
            </div>

            <div className='mm-target w-1' onClick={select} />

            <div className='space-y-1'>
                {property.subpaths.map(subpath => (
                    <PropertyDisplay
                        key={subpath.name.toString()}
                        property={subpath}
                        selected={selected}
                        onClick={onClick ? innerOnClick : undefined}
                        collapsedLevel={collapsedLevel !== undefined ? collapsedLevel - 1 : undefined}
                    />
                ))}
            </div>

            <div className='mm-target col-span-2 w-fit px-1' onClick={select}>
                {'}'}
            </div>
        </div>
    );
};
