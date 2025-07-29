import { Button, ButtonGroup } from '@heroui/react';
import { AVAILABLE_VIEWS } from './Views';
import type { DatasourceType } from '@/types/Datasource';
import type { View } from '@/types/adminer/View';
import type { AdminerFilterQueryStateAction } from '@/components/adminer/adminerReducer';
import { type Dispatch } from 'react';

type ViewMenuProps = {
    /** The type of selected datasource. */
    datasourceType: DatasourceType;
    /** Current view. */
    view: View;
    /** A function for state updating. */
    dispatch: Dispatch<AdminerFilterQueryStateAction>;
};

/**
 * Component for selecting view
 */
export function ViewMenu({ datasourceType, view, dispatch }: ViewMenuProps) {
    const availableViews = AVAILABLE_VIEWS[datasourceType];

    if (availableViews.length < 2)
        return null;

    return (
        <ButtonGroup
            size='sm'
            className='max-w-m mx-2'
        >
            {availableViews.map(availableView => (
                <Button
                    size='sm'
                    variant={availableView === view ? 'solid' : 'ghost'}
                    key={availableView}
                    onPress={() => dispatch({ type:'view', newView: availableView })}
                >
                    {availableView}
                </Button>
            ),
            )}
        </ButtonGroup>
    );
}
