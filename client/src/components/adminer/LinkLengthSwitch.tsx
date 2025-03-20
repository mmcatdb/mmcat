import { Switch } from '@nextui-org/react';
import { usePreferences } from '@/components/PreferencesProvider';

export function LinkLengthSwitch() {
    const { preferences, setPreferences } = usePreferences();
    const { adminerShortLinks: adminerShortRefs } = preferences;

    const handleChange = (isChecked: boolean) => {
        setPreferences({ ...preferences, adminerShortLinks: isChecked });
    };

    return (
        <Switch
            className='mx-2'
            aria-label='Short names'
            isSelected={adminerShortRefs}
            onChange={e => handleChange(e.target.checked)}
            size='sm'
        >
            <p className='text-small'>Short names</p>
        </Switch>
    );
}
