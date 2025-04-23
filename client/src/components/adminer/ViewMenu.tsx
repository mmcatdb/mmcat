import { Button, ButtonGroup } from '@nextui-org/react';
import { AVAILABLE_VIEWS } from './Views';
import type { DatasourceType } from '@/types/datasource';
import type { View } from '@/types/adminer/View';
import type { AdminerFilterQueryStateAction } from '@/types/adminer/ReducerTypes';

type ViewMenuProps = Readonly<{
    datasourceType: DatasourceType;
    view: View;
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

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
                    aria-label='Query type'
                    type='submit'
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
