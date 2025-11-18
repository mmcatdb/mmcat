import { useMemo, useState } from 'react';
import { Button } from '@heroui/react';
import { usePreferences } from '@/components/PreferencesProvider';
import { ReferenceDisplay } from '@/components/adminer/dataView/ReferenceDisplay';
import type { Datasource } from '@/types/Datasource';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';
import { cn } from '@/components/utils';

type DocumentDisplayProps = {
    /** The name of the property. Will be displayed unless undefined or hidden. */
    property: string | undefined;
    /** Explicitly hides the name of the property. */
    hideProperty?: boolean;
    /** The value the for given property. */
    value: unknown;
    /** References from and to the current kind. */
    kindReferences: KindReference[];
    /** Name of the current kind. */
    kind: string;
    /** The id of selected datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
};

/**
 * Component for displaying data recursively in a JSON-like structure
 */
export function DocumentDisplay({ property, hideProperty, value, kindReferences, kind, datasourceId, datasources }: DocumentDisplayProps) {
    const { adminerShortLinks } = usePreferences().preferences;
    const [ isOpen, setIsOpen ] = useState(true);

    const parsedValue = useMemo(() => computeParsedValue(value), [ value ]);

    if (typeof parsedValue === 'object' && parsedValue && !Array.isArray(parsedValue)) {
        // If parsedValue is an object, create another unordered list for its key-value pairs
        const entries = Object.entries(parsedValue);

        return (
            <div className='group leading-5'>
                {!hideProperty && property !== undefined && (
                    <span className='text-secondary-600'>
                        <span className='mr-3 font-bold'>{property}:</span>
                    </span>
                )}

                {entries.length === 0 ? '{ }' : (<>
                    {'{'}

                    {/* Keep the components rendered so their open state will remain. */}
                    <ul className={cn('pl-8', !isOpen && 'hidden')}>
                        {entries.map(([ key, val ]) => (
                            <li key={key}>
                                <DocumentDisplay
                                    property={key}
                                    value={val as unknown}
                                    kindReferences={kindReferences}
                                    kind={kind}
                                    datasourceId={datasourceId}
                                    datasources={datasources}
                                />
                            </li>
                        ))}
                    </ul>

                    {isOpen ? '}' : ' }'}

                    <ToggleOpenButton isOpen={isOpen} setIsOpen={setIsOpen} />
                </>)}
            </div>
        );
    }

    if (Array.isArray(parsedValue)) {
        // If parsedValue is an array, create a list for each item
        return (
            <div className='group leading-5'>
                {!hideProperty && property !== undefined && (
                    <span className='text-secondary-600'>
                        <span className='mr-3 font-bold'>{property}:</span>
                    </span>
                )}

                {parsedValue.length === 0 ? '[ ]' : (<>
                    {'['}

                    {/* Keep the components rendered so their open state will remain. */}
                    <ul className={cn('pl-8', !isOpen && 'hidden')}>
                        {parsedValue.map((item, index) => (
                            <li key={index}>
                                <DocumentDisplay
                                    property={undefined}
                                    value={item as unknown}
                                    kindReferences={kindReferences}
                                    kind={kind}
                                    datasourceId={datasourceId}
                                    datasources={datasources}
                                />
                            </li>
                        ))}
                    </ul>

                    {isOpen ? ']' : ' ]'}

                    <ToggleOpenButton isOpen={isOpen} setIsOpen={setIsOpen} />
                </>)}
            </div>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return (
        <div className='leading-5'>
            {!hideProperty && property !== undefined && (
                <span className='text-secondary-600'>
                    <span className='mr-3 font-bold'>{property}:</span>
                </span>
            )}

            <span className={getColor(parsedValue)}>
                {String(parsedValue)}
            </span>

            {property && kindReferences.length > 0 && kindReferences.some(ref => ref.fromProperty === property) && (
                <div className={cn('pl-4 leading-5', adminerShortLinks && 'inline')}>
                    <ReferenceDisplay references={kindReferences} data={({ [property]: parsedValue as string })} property={property} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                </div>
            )}
        </div>
    );
}

const patterns: [ RegExp, string ][] = [
    [ /^\d+:([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}):\d+$/, 'text-secondary-600' ], // uuid
    [ /^(true|false)$/i, 'text-danger-400' ], // boolean
    [ /^-?\d+(\.\d+)?$/, 'text-success-600' ], // number
];

function getColor(value: unknown): string {
    if (value === null)
        return 'text-primary-500';

    for (const [ pattern, color ] of patterns) {
        if (pattern.test(String(value)))
            return color;
    }

    return 'text-foreground';
}

function computeParsedValue(value: unknown): unknown {
    try {
        return JSON.parse(value as string);
    }
    catch {
        // The value cannot be parsed to JSON
        return value;
    }
}

function ToggleOpenButton({ isOpen, setIsOpen }: { isOpen: boolean, setIsOpen: (open: boolean) => void }) {
    return (
        <Button
            className={cn('mx-1 h-5 px-1 min-w-5', isOpen && 'opacity-0 group-hover:opacity-100')}
            variant='ghost'
            onPress={() => setIsOpen(!isOpen)}
        >
            {isOpen ? '-' : '+'}
        </Button>
    );

}
