import { Select, SelectItem } from '@nextui-org/react';
import { AVAILABLE_VIEWS } from './Views';
import type { DatasourceType } from '@/types/datasource';
import type { View } from '@/types/adminer/View';
import type { AdminerStateAction } from '@/types/adminer/Reducer';

type ViewMenuProps = Readonly<{
    datasourceType: DatasourceType;
    view: View;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function ViewMenu({ datasourceType, view, dispatch }: ViewMenuProps) {
    const availableViews = AVAILABLE_VIEWS[datasourceType];

    if (availableViews.length < 2)
        return null;

    return (
        <Select
            items={availableViews.entries()}
            label='View'
            placeholder='Select datasource'
            className='max-w-xs'
            selectedKeys={[ view ]}
        >
            {availableViews.map(view => (
                <SelectItem
                    key={view}
                    onPress={() => dispatch({ type:'view', newView: view })}
                >
                    {view}
                </SelectItem>
            ))}
        </Select>
    );
}
