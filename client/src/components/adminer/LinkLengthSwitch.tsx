import { Switch } from '@nextui-org/react';
import { usePreferences } from '@/components/PreferencesProvider';

export function LinkLengthSwitch() {
    const { preferences, setPreferences } = usePreferences();
    const { adminerShortLinks: adminerShortRefs } = preferences;

    return (
        <Switch
            className='ml-auto'
            aria-label='Short names'
            isSelected={adminerShortRefs}
            onChange={e => setPreferences({ ...preferences, adminerShortLinks: e.target.checked })}
            size='sm'
        >
            <p className='text-small'>Short names</p>
        </Switch>
    );
}
