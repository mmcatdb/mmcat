import { type PatternTree } from '@/types/query';
import { Signature } from '@/types/identifiers';
import { cn } from '@/components/common/utils';
import { useMemo, useState } from 'react';
import { SignatureDisplay } from '../common/categoryComponents';

type PatternTreeDisplayProps = {
    pattern: PatternTree;
    signature?: Signature;
    className?: string;
    /** By default, everything is expanded except for the root. */
    isExpandedDefault?: boolean;
};

export function PatternTreeDisplay({ pattern, signature, className, isExpandedDefault }: PatternTreeDisplayProps) {
    const isRoot = !signature;
    const [ isExpanded, setIsExpanded ] = useState(isExpandedDefault ?? !isRoot);

    const children = useMemo(() => Object.entries(pattern.children).map(([ signature, child ]) => ({ signature: Signature.fromResponse(signature), child })), [ pattern.children ]);
    const isEmpty = children.length === 0;
    const onClick = isEmpty ? undefined : () => setIsExpanded(!isExpanded);

    return (
        <div className={cn(
            'grid grid-cols-[auto_minmax(0,1fr)] gap-y-1 gap-x-3',
            !isEmpty && 'mm-tree-selectable',
            isRoot && 'leading-5',
            className,
        )}>
            <div className={cn('mm-target col-span-2 w-fit px-1 flex items-center', !isEmpty && 'cursor-pointer')} onClick={onClick}>
                {!isRoot && <SignatureDisplay signature={signature} />}
                <span className='font-mono'>
                    {!isRoot && ': '}
                    {pattern.variable.name}
                    {!isEmpty && (isExpanded ? ' {' : ' { ... }')}
                </span>
            </div>

            {!isEmpty && isExpanded && (
                <div className='mm-target w-1 cursor-pointer' onClick={onClick} />
            )}

            {/* We want to always render the children to keep their isExpanded state. */}
            <div className={cn('space-y-1', !isExpanded && 'hidden')}>
                {children.map(({ signature, child }) => (
                    <PatternTreeDisplay
                        key={signature.toString()}
                        pattern={child}
                        signature={signature}
                    />
                ))}
            </div>

            {!isEmpty && isExpanded && (
                <div className='mm-target col-span-2 w-fit px-1 cursor-pointer' onClick={onClick}>{'}'}</div>
            )}
        </div>
    );
};
