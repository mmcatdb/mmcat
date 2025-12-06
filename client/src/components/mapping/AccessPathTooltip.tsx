import { Tooltip } from '@heroui/react';
import type { RootProperty } from '@/types/mapping';
import { usePreferences } from '../context/PreferencesProvider';

export function AccessPathTooltip({ accessPath, text }: { accessPath: RootProperty, text: string }) {
    const { preferences } = usePreferences();

    return (
        <Tooltip
            content={
                <pre className='text-sm p-2'>
                    {preferences.accessPathShortForm ? accessPath.toStringShortForm() : accessPath.toString()}
                </pre>
            }
            disableAnimation
        >
            <span className='underline cursor-pointer'>
                {text}
            </span>
        </Tooltip>
    );
}
