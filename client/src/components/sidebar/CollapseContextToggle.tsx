import { Button } from '@heroui/react';
import { usePreferences } from '@/components/PreferencesProvider';
import { Tooltip } from '@/components/common';
import { BsWindowSidebar } from 'react-icons/bs';

/**
 * Renders a button to toggle the sidebar's collapsed state.
 */
export function CollapseContextToggle() {
    const { preferences, setPreferences } = usePreferences();
    const { isCollapsed } = preferences;
    const label = 'Toggle Side Bar';

    return (
        <Tooltip content={label} placement='right'>
            <Button
                isIconOnly
                aria-label={label}
                onPress={() => setPreferences({ ...preferences, isCollapsed: !isCollapsed })}
                variant='faded'
            >
                <BsWindowSidebar size={22} />
            </Button>
        </Tooltip>
    );
}
