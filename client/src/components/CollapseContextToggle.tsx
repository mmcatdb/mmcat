import { Button } from '@nextui-org/react';
import { usePreferences } from './PreferencesProvider';
import { Tooltip } from './common';
import { BsWindowSidebar } from 'react-icons/bs';

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
