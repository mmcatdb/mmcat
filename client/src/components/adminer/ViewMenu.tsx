import { Select, SelectItem } from '@nextui-org/react';
import { DatasourceType } from '@/types/datasource';
import { View } from '@/types/adminer/View';
import { type AdminerStateAction } from '@/types/adminer/Reducer';

type ViewMenuProps = Readonly<{
    datasourceType: DatasourceType;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function ViewMenu({ datasourceType, dispatch }: ViewMenuProps) {
    return (
        (datasourceType == DatasourceType.neo4j) && (
            <Select
                label='View'
                placeholder='Select datasource'
                className='max-w-xs'
            >
                <SelectItem
                    key={'table'}
                    onPress={() => dispatch({ type:'view', newView: View.table })}
                >
                Table
                </SelectItem>
                <SelectItem
                    key={'document'}
                    onPress={() => dispatch({ type:'view', newView: View.document })}
                >
                JSON
                </SelectItem>
            </Select>
        )
    );
}
