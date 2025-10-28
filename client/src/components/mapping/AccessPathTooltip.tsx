import { Tooltip } from '@heroui/react';
import type { RootProperty } from '@/types/mapping';

export function AccessPathTooltip({ accessPath, text }: { accessPath: RootProperty, text: string }) {
    return (
        <Tooltip
            content={
                <pre className='text-sm p-2'>
                    {accessPath.toString()}
                </pre>
            }
            placement='top-start'
        >
            <span className='underline cursor-pointer'>
                {text}
            </span>
        </Tooltip>
    );
}
