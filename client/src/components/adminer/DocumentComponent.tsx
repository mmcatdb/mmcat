import { useState } from 'react';
import { Button } from '@nextui-org/react';
import { ReferenceComponent } from '@/components/adminer/ReferenceComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

function getColor(value: string): string {
    if (!Number.isNaN(Number.parseFloat(value)))
        return 'text-success-600';

    if (value.toLocaleLowerCase() === 'true' || value.toLocaleLowerCase() === 'false')
        return 'text-danger-400';

    return 'text-foreground';
}

type DocumentComponentProps = Readonly<{
    valueKey: unknown;
    value: unknown;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DocumentComponent({ valueKey, value, kindReferences, kind, datasourceId, datasources }: DocumentComponentProps) {
    const [ isOpen, setIsOpen ] = useState(true);

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

    if (typeof value === 'object' && value && !Array.isArray(value)) {
        // If value is an object, create another unordered list for its key-value pairs
        return (
            <span className='group'>
                {'{'}

                <ul>
                    {Object.entries(value)
                        .filter(([ key ]) => key !== '_id')
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

    if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        return (
            <span className='group'>
                {'['}

                <div className='ml-4'>
                    <ul>
                        {value.map((item, index) => (
                            <li className='ps-4' key={index}>
                                <DocumentComponent valueKey={null} value={item as unknown} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                                {(index != value.length - 1) && ',' }
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
            <span className={getColor(String(value))}>
                {String(value)}
            </span>
            {valueKey !== null
                && kindReferences.length > 0
                && kindReferences.some(ref => ref.referencingProperty === valueKey)
                && (
                    <div className='ps-4'>
                        <ReferenceComponent references={kindReferences} data={({ [valueKey as string]: value as string })} propertyName={valueKey as string} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                    </div>
                )}
        </span>
    );
}
