import { useState } from 'react';
import { Button } from '@nextui-org/react';
import { ReferenceComponent } from '@/components/adminer/ReferenceComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

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
                className='mx-2 h-5'
                variant='ghost'
                onPress={() => setIsOpen(true)}>
                Open
            </Button>
        );
    }

    if (typeof value === 'object' && value && !Array.isArray(value)) {
        // If value is an object, create another unordered list for its key-value pairs
        return (
            <span>
                {'{'}

                <ul>
                    {Object.entries(value)
                        .filter(([ key ]) => key !== '_id')
                        .map(([ key, val ]) => (
                            <li className='ps-8' key={key}>
                                <strong className='mr-3'>{key}:</strong>
                                <DocumentComponent valueKey={key} value={val as unknown} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                            </li>
                        ))}
                </ul>

                {'}'}

                <Button
                    className='mx-2 h-5'
                    variant='ghost'
                    onPress={() => setIsOpen(false)}>
                    Close
                </Button>
            </span>
        );
    }

    if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        return (
            <span>
                {'['}

                <div className='ml-4'>
                    <ul>
                        {value.map((item, index) => (
                            <li className='ps-4' key={index}>
                                <DocumentComponent valueKey={null} value={item as unknown} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                            </li>
                        ))}
                    </ul>
                </div>

                {']'}

                <Button
                    className='mx-2 h-5'
                    variant='ghost'
                    onPress={() => setIsOpen(false)}>
                    Close
                </Button>
            </span>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return (
        <span>
            {String(value)}
            <div className='ps-4'>
                {valueKey !== null
                    && kindReferences.length > 0
                    && kindReferences.some(ref => ref.referencingProperty === valueKey)
                    && (<ReferenceComponent references={kindReferences} data={({ [valueKey as string]: value as string })} propertyName={valueKey as string} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                    )}
            </div>
        </span>
    );
}
