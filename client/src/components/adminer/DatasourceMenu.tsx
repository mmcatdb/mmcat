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
    const sources = datasources
        .filter(item =>
            item.type === DatasourceType.postgresql ||
            item.type === DatasourceType.mongodb ||
            item.type === DatasourceType.neo4j,
        );

    if (sources.length > 0) {
        return (
            <Select
                items={sources}
                label='Datasource'
                placeholder='Select datasource'
                className='max-w-xs'
                selectedKeys={ datasourceId ? [ datasourceId ] : [] }
            >
                {item => (
                    <SelectItem
                        key={item.id}
                        onPress={() => dispatch({ type:'datasource', newDatasource: item })}
                    >
                        {item.label}
                    </SelectItem>
                )}
            </Select>
        );
    }

    return (
        <p>No datasources to display.</p>
    );
}
