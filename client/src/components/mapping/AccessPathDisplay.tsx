import { useCallback } from 'react';
import { type NamePath, NamePathBuilder } from '@/types/identifiers';
import { type AccessPath, type ParentProperty, type RootProperty, type SimpleProperty } from '@/types/mapping';
import { cn } from '@/components/utils';

/** Either an access path or editable access path. */
type Property = ParentProperty | SimpleProperty | AccessPath;

type AccessPathDisplayProps = {
    property: AccessPath | RootProperty;
    selected?: Property;
    setSelected?: (path: NamePath) => void;
    className?: string;
};

export function AccessPathDisplay({ property, selected, setSelected, className }: AccessPathDisplayProps) {
    const innerSetSelected = useCallback((builder: NamePathBuilder) => {
        // The first element is going to be the root property, so we shift it off.
        // We don't take kindly to your types in here!
        setSelected?.(builder.shift().build());
    }, [ setSelected ]);

    return (
        <div className={cn('p-3 rounded-lg bg-default-100 leading-5', className)}>
            <ParentPropertyDisplay
                property={property}
                selected={selected}
                setSelected={innerSetSelected}
            />
        </div>
    );
}

type ParentPropertyDisplayProps = {
    property: AccessPath | ParentProperty;
    selected: Property | undefined;
    setSelected: (builder: NamePathBuilder) => void;
};

function ParentPropertyDisplay({ property, selected, setSelected }: ParentPropertyDisplayProps) {
    const innerSetSelected = useCallback((builder: NamePathBuilder) => {
        builder.prepend(property.name);
        setSelected(builder);
    }, [ setSelected, property.name ]);

    function select() {
        setSelected(new NamePathBuilder(property.name));
    }

    const isSelected = property === selected;

    return (
        <div className={cn('mm-access-path grid grid-cols-[auto_minmax(0,1fr)] gap-y-1 gap-x-3', isSelected && 'mm-access-path-selected')}>
            <div className='mm-target col-span-2 w-fit px-1 space-x-2' onClick={select}>
                <span>{property.name.toString()}:</span>
                <span>{property.signature.toString()}</span>
                <span>{'{'}</span>
            </div>

            <div className='mm-target w-1' onClick={select} />

            <div className='space-y-1'>
                {property.subpaths.map(subpath => (
                    ('subpaths' in subpath && subpath.subpaths.length > 0) ? (
                        <ParentPropertyDisplay
                            key={subpath.name.toString()}
                            property={subpath}
                            selected={selected}
                            setSelected={innerSetSelected}
                        />
                    ) : (
                        <SimplePropertyDisplay
                            key={subpath.name.toString()}
                            property={subpath}
                            selected={selected}
                            setSelected={innerSetSelected}
                        />
                    )
                ))}
            </div>

            <div className='mm-target col-span-2 w-fit px-1' onClick={select}>
                {'}'}
            </div>
        </div>
    );
}

function SimplePropertyDisplay({ property, selected, setSelected }: {
    property: AccessPath | SimpleProperty;
    selected: Property | undefined;
    setSelected: (builder: NamePathBuilder) => void;
}) {
    function select() {
        setSelected(new NamePathBuilder(property.name));
    }

    const isSelected = property === selected;

    return (
        <div className={cn('mm-access-path', isSelected && 'mm-access-path-selected')}>
            <div className='mm-target w-fit px-1 space-x-2' onClick={select}>
                <span>{property.name.toString()}:</span>
                <span>{property.signature.toString()}</span>
            </div>
        </div>
    );
}
