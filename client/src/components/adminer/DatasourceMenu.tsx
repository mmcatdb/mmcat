import { Select, SelectItem } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { AdminerStateAction } from '@/types/adminer/Reducer';
import type { Id } from '@/types/id';

type DatasourceMenuProps = Readonly<{
    dispatch: React.Dispatch<AdminerStateAction>;
    datasourceId: Id | undefined;
    datasources: Datasource[];
}>;

export function DatasourceMenu({ dispatch, datasourceId, datasources }: DatasourceMenuProps) {
    return (
        <Select
            label='Datasource'
            placeholder='Select datasource'
            className='max-w-xs'
            defaultSelectedKeys={ datasourceId ? [ datasourceId ] : [] }
        >
            {datasources
                .filter((item) =>
                    item.type === DatasourceType.postgresql ||
                    item.type === DatasourceType.mongodb ||
                    item.type === DatasourceType.neo4j,
                )
                .map((item) => (
                    <SelectItem
                        key={item.id}
                        onPress={() => dispatch({ type:'datasource', newDatasource: item })}
                    >
                        {item.label}
                    </SelectItem>
                ))}
        </Select>
    );
}
