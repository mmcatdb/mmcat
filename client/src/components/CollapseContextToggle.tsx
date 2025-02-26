import { Button } from '@nextui-org/react';
import { usePreferences } from './PreferencesProvider';
import { Tooltip } from './common';
import { BsWindowSidebar } from 'react-icons/bs';

export function CollapseContextToggle() {
    const { preferences, setPreferences } = usePreferences();
    const { isCollapsed } = preferences;
    const label = 'Toggle Primary Side Bar';

    return (
        <Tooltip content={label}>
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
