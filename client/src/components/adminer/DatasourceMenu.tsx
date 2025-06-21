import { Select, SelectItem } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';

type DatasourceMenuProps = Readonly<{
    /** Function for updating 'datasource' parameter. */
    setDatasource: React.Dispatch<React.SetStateAction<Datasource | undefined>>;
    /** The selected datasource. */
    datasource: Datasource | undefined;
    /** All active datasources. */
    datasources: Datasource[];
}>;

/**
 * Component for selecting the datasource to fetch the data from
 */
export function DatasourceMenu({ setDatasource, datasource, datasources }: DatasourceMenuProps) {
    const sources = datasources
        .filter(item =>
            [ DatasourceType.postgresql, DatasourceType.mongodb, DatasourceType.neo4j ].includes(item.type),
        );

    if (sources.length === 0) {
        return (
            <p>No datasources to display.</p>
        );
    }

    return (
        <Select
            items={sources}
            aria-label='Datasource'
            labelPlacement='outside-left'
            classNames={
                { label:'sr-only' }
            }
            size='sm'
            placeholder='Select datasource'
            className='max-w-xs'
            selectedKeys={ datasource?.id ? [ datasource.id ] : [] }
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
