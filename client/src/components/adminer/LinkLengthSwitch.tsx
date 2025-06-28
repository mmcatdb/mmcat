import { Switch } from '@heroui/react';
import { usePreferences } from '@/components/PreferencesProvider';

/**
 * Switch for setting the length of names of datasources, kinds and properties used in links
 */
export function LinkLengthSwitch() {
    const { preferences, setPreferences } = usePreferences();
    const { adminerShortLinks: adminerShortRefs } = preferences;

    return (
        <Switch
            className='ml-auto'
            isSelected={adminerShortRefs}
            onChange={e => setPreferences({ ...preferences, adminerShortLinks: e.target.checked })}
            size='sm'
        >
            <p className='text-small'>Short names</p>
        </Switch>
    );
}
