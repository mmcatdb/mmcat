import { Tooltip } from '@nextui-org/react';
import type { RootProperty } from '@/types/accessPath/basic';

export function AccessPathTooltip({ accessPath }: { accessPath: RootProperty }) {
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
                View Access Path
            </span>
        </Tooltip>
    );
}
