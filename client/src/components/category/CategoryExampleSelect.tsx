import { Example } from '@/types/schema';
import { Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react';
import { FaPlus } from 'react-icons/fa';
import { SpinnerButton } from '../common/components';

type CategoryExampleSelectProps = {
    isFetching: boolean | undefined;
    onSelect: (example: Example) => void;
};

export function CategoryExampleSelect({ isFetching, onSelect }: CategoryExampleSelectProps) {
    return (
        <Dropdown>
            <DropdownTrigger>
                <SpinnerButton color='secondary' variant='flat' isFetching={isFetching}>
                    <FaPlus className='size-4' /> Example
                </SpinnerButton>
            </DropdownTrigger>
            <DropdownMenu onAction={key => onSelect(key as Example)}>
                {availableExamples.map(({ example, label }) => (
                    <DropdownItem key={example}>
                        {label}
                    </DropdownItem>
                ))}
            </DropdownMenu>
        </Dropdown>
    );
}

const availableExamples: { example: Example, label: string }[] = [
    { example: Example.basic, label: 'Basic' },
    { example: Example.adminer, label: 'Adminer' },
    // Not enabled right now because it nees a version paremeter.
    // { example: Example.queryEvolution, label: 'Query Evolution' },
    // { example: Example.inference, label: 'Inference' },
    // { example: Example.tpch, label: 'TPC-H' },
    { example: Example.adaptation, label: 'Adaptation' },
];
