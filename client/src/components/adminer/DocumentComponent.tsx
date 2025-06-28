import { useMemo, useState } from 'react';
import { Button } from '@heroui/react';
import { usePreferences } from '@/components/PreferencesProvider';
import { ReferenceComponent } from '@/components/adminer/ReferenceComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DocumentComponentProps = {
    /** The name of the property. */
    valueKey: string | undefined;
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
export function DocumentComponent({ valueKey, value, kindReferences, kind, datasourceId, datasources }: DocumentComponentProps) {
    const { adminerShortLinks } = usePreferences().preferences;
    const [ isOpen, setIsOpen ] = useState(true);

    const parsedValue = useMemo(() => {
        return computeParsedValue(value);
    }, [ value ]);

    if (!isOpen) {
        return (
            <Button
                className='m-1 h-5 px-1 min-w-5'
                variant='ghost'
                onPress={() => setIsOpen(true)}>
                +
            </Button>
        );
    }

    if (typeof parsedValue === 'object' && parsedValue && !Array.isArray(parsedValue)) {
        // If parsedValue is an object, create another unordered list for its key-value pairs
        return (
            <span className='group'>
                {'{'}

                <ul>
                    {Object.entries(parsedValue)
                        .map(([ key, val ]) => (
                            <li className='ps-8' key={key}>
                                <span className='text-secondary-600'>
                                    <strong className='mr-3'>{key}:</strong>
                                </span>
                                <DocumentComponent valueKey={key} value={val as unknown} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                            </li>
                        ))}
                </ul>

                {'}'}

                <Button
                    className='m-1 h-5 px-1 min-w-5 opacity-0 group-hover:opacity-100'
                    variant='ghost'
                    onPress={() => setIsOpen(false)}>
                    -
                </Button>
            </span>
        );
    }

    if (Array.isArray(parsedValue)) {
        // If parsedValue is an array, create a list for each item
        return (
            <span className='group'>
                {'['}

                <div className='ml-4'>
                    <ul>
                        {parsedValue.map((item, index) => (
                            <li className='ps-4' key={index}>
                                <DocumentComponent valueKey={undefined} value={item as unknown} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                                {(index != parsedValue.length - 1) && ',' }
                            </li>
                        ))}
                    </ul>
                </div>

                {']'}

                <Button
                    className='m-1 h-5 px-1 min-w-5 opacity-0 group-hover:opacity-100'
                    variant='ghost'
                    onPress={() => setIsOpen(false)}>
                    -
                </Button>
            </span>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return (
        <span>
            <span className={getColor(String(parsedValue))}>
                {parsedValue === null ? '' : String(parsedValue)}
            </span>
            {valueKey
                && kindReferences.length > 0
                && kindReferences.some(ref => ref.fromProperty === valueKey)
                && (
                    adminerShortLinks ? (
                        <span className='ps-4'>
                            <ReferenceComponent references={kindReferences} data={({ [valueKey]: parsedValue as string })} propertyName={valueKey} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                        </span>
                    ) : (
                        <div className='ps-4'>
                            <ReferenceComponent references={kindReferences} data={({ [valueKey]: parsedValue as string })} propertyName={valueKey} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                        </div>
                    )
                )}
        </span>
    );
}

function getColor(value: string): string {
    const patterns = {
        boolean: /^(true|false)$/i,
        number: /^-?\d+(\.\d+)?$/,
        elementId: /^\d+:([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}):\d+$/,
    };

    if (patterns.elementId.test(value))
        return 'text-secondary-600';

    if (patterns.boolean.test(value))
        return 'text-danger-400';

    if (patterns.number.test(value))
        return 'text-success-600';

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
