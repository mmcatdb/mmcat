import { useState } from 'react';
import { Button } from '@nextui-org/react';

type ListComponentProps = Readonly<{
    value: unknown;
    depth: number;
}>;

export function ListComponent({ value, depth }: ListComponentProps) {
    const [ isOpen, setIsOpen ] = useState(true);

    if (!isOpen) {
        return (
            <Button onPress={() => setIsOpen(true)}>Open</Button>
        );
    }

    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
        // If value is an object, create another unordered list for its key-value pairs
        const len = Object.entries(value).length;
        return (
            <ul className='ps-8' onClick={(e) => {
                e.stopPropagation(); setIsOpen(false);
            }}>
                {len > 1 && depth > 0 && '{'}
                {Object.entries(value).map(([ key, val ]) => (
                    <li className='ps-8' key={key}>
                        <strong>{key}:</strong> <ListComponent value={val as unknown} depth={depth + 1} />
                    </li>
                ))}
                {len > 1 && depth > 0 && '}'}
            </ul>
        );
    }

    if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        const len = value.length;
        return (
            <ul className='ps8' onClick={(e) => {
                e.stopPropagation(); setIsOpen(false);
            }}>
                {len > 1 && depth > 0 && '{'}
                {value.map((item, index) => (
                    <li className='ps-8' key={index}><ListComponent value={item as unknown} depth={depth + 1} /></li>
                ))}
                {len > 1 && depth > 0 && '}'}
            </ul>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return <span>{String(value)}</span>;
}
