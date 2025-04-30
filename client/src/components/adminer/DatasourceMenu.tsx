import { Select, SelectItem } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { Id } from '@/types/id';

type DatasourceMenuProps = Readonly<{
    setDatasource: React.Dispatch<React.SetStateAction<Datasource | undefined>>;
    // FIXME Toto je trochu zvláštní. Předáváte datasourceId, ale setDatasource je funkce, která nastavuje datasource. Buď v obou případech pracujte s Datasource, nebo s datasourceId (a potom na AdminerPage pomocí useMemo najděte datasource podle id).
    datasourceId: Id | undefined;
    datasources: Datasource[];
}>;

export function DatasourceMenu({ setDatasource, datasourceId, datasources }: DatasourceMenuProps) {
    const sources = datasources
        .filter(item =>
            // FIXME Existuje [ ... ].includes()
            item.type === DatasourceType.postgresql ||
            item.type === DatasourceType.mongodb ||
            item.type === DatasourceType.neo4j,
        );

    // FIXME Zvolil bych early return - pokud je sources.length === 0, tak to rovnou vrátí prázdný element. Jinak se pokračuje se selectem.
    if (sources.length > 0) {
        return (
            <Select
                items={sources}
                // FIXME Opět bych spíš použil label než aria-label.
                aria-label='Datasource'
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
