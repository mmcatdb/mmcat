import { useState } from 'react';
import { Button } from '@nextui-org/react';

type DocumentComponentProps = Readonly<{
    value: unknown;
    depth: number;
}>;

export function DocumentComponent({ value, depth }: DocumentComponentProps) {
    const [ isOpen, setIsOpen ] = useState(true);

    if (!isOpen) {
        return (
            <Button onPress={() => setIsOpen(true)}>Open</Button>
        );
    }

    if (typeof value === 'object' && value && !Array.isArray(value)) {
        // If value is an object, create another unordered list for its key-value pairs
        const len = Object.entries(value).length;
        return (
            <span className='mx-3'>
                {depth > 0 && '{'}

                {/* If length is 1, just render a single line, otherwise render the entire list */}
                {len === 1 ? (
                    <span className='mx-3'>
                        <strong>{Object.keys(value)[0]}:</strong>
                        <DocumentComponent value={Object.values(value)[0] as unknown} depth={depth + 1} />
                    </span>
                ) : (
                    <ul className='ps-8' onClick={(e) => {
                        e.stopPropagation(); setIsOpen(false);
                    }}>
                        {Object.entries(value).map(([ key, val ]) => (
                            <li className='ps-8' key={key}>
                                <strong>{key}:</strong>
                                <DocumentComponent value={val as unknown} depth={depth + 1} />
                            </li>
                        ))}
                    </ul>
                )}

                {depth > 0 && '}'}
            </span>
        );
    }

    if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        const len = value.length;
        return (
            <span className='mx-3'>
                {depth > 0 && '{'}

                {/* If length is 1, just render a single line, otherwise render the entire list */}
                {len === 1 ? (
                    <span className='mx-3'>
                        <DocumentComponent value={value[0] as unknown} depth={depth + 1} />
                    </span>
                ) : (
                    <ul className='ps8' onClick={(e) => {
                        e.stopPropagation(); setIsOpen(false);
                    }}>
                        {value.map((item, index) => (
                            <li className='ps-8' key={index}><DocumentComponent value={item as unknown} depth={depth + 1} /></li>
                        ))}
                    </ul>
                )}
                {depth > 0 && '}'}
            </span>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return <span>{String(value)}</span>;
}
