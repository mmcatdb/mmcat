import { type ResultStructure } from '@/types/query';
import { cn } from '@/components/common/utils';
import { useState } from 'react';
import { SignatureDisplay } from '../common/categoryComponents';

type ResultStructureDisplayProps = {
    structure: ResultStructure;
    className?: string;
    /** By default, everything is expanded except for the root. */
    isExpandedDefault?: boolean;
};

export function ResultStructureDisplay({ structure, className, isExpandedDefault }: ResultStructureDisplayProps) {
    const isRoot = !structure.signatureFromParent;
    const [ isExpanded, setIsExpanded ] = useState(isExpandedDefault ?? !isRoot);

    const isEmpty = structure.children.length === 0;
    const onClick = isEmpty ? undefined : () => setIsExpanded(!isExpanded);

    return (
        <div className={cn(
            'grid grid-cols-[auto_minmax(0,1fr)] gap-y-1 gap-x-3',
            !isEmpty && 'mm-tree-selectable',
            isRoot && 'leading-5',
            className,
        )}>
            <div className={cn('mm-target col-span-2 w-fit px-1 flex items-center', !isEmpty && 'cursor-pointer')} onClick={onClick}>
                {!isRoot && <SignatureDisplay signature={structure.signatureFromParent} />}
                <span className='font-mono'>
                    {!isRoot && ': '}
                    {structure.displayName}
                    {structure.name !== structure.variable.name && (
                        // The variable name is usually the same as the structure name, so we only show it if they differ.
                        <span className='text-foreground-500'>{` (?${structure.variable.name})`}</span>
                    )}
                    {!isEmpty && (isExpanded ? ' {' : ' { ... }')}
                </span>
            </div>

            {!isEmpty && isExpanded && (
                <div className='mm-target w-1 cursor-pointer' onClick={onClick} />
            )}

            {/* We want to always render the children to keep their isExpanded state. */}
            <div className={cn('space-y-1', !isExpanded && 'hidden')}>
                {structure.children.map(subpath => (
                    <ResultStructureDisplay
                        key={subpath.name}
                        structure={subpath}
                    />
                ))}
            </div>

            {!isEmpty && isExpanded && (
                <div className='mm-target col-span-2 w-fit px-1 cursor-pointer' onClick={onClick}>{'}'}</div>
            )}
        </div>
    );
};
