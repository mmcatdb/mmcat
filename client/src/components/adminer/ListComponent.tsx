import { useState } from 'react';
import { Button } from '@nextui-org/react';

type ListComponentProps = Readonly<{
    value: unknown;
}>;

export function ListComponent({ value }: ListComponentProps) {
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
            <ul className='ps-8' onClick={(e) => { e.stopPropagation(); setIsOpen(false); }}>
                {len > 1 && '{'}
                {Object.entries(value).map(([ key, val ]) => (
                    <li key={key}>
                        <strong>{key}:</strong> <ListComponent value={val} />
                    </li>
                ))}
                {len > 1 && '}'}
            </ul>
        );
    }
    
    if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        const len = value.length;
        return (
            <ul className="ps8" onClick={(e) => { e.stopPropagation(); setIsOpen(false); }}>
                {len > 1 && '{'}
                {value.map((item, idx) => (
                    <li key={idx}><ListComponent value={item} /></li>
                ))}
                {len > 1 && '}'}
            </ul>
        );
    }

    // For primitive values (string, number, etc.), return them as a string
    return <span>{String(value)}</span>;
};