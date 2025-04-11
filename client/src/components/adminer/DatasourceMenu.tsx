import { Select, SelectItem } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { Id } from '@/types/id';

type DatasourceMenuProps = Readonly<{
    setDatasource: React.Dispatch<React.SetStateAction<Datasource | undefined>>;
    datasourceId: Id | undefined;
    datasources: Datasource[];
}>;

export function DatasourceMenu({ setDatasource, datasourceId, datasources }: DatasourceMenuProps) {
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
                labelPlacement='outside-left'
                classNames={
                    { label:'sr-only' }
                }
                size='sm'
                placeholder='Select datasource'
                className='max-w-xs'
                selectedKeys={ datasourceId ? [ datasourceId ] : [] }
            >
                {item => (
                    <SelectItem
                        key={item.id}
                        onPress={() => setDatasource(item)}
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
