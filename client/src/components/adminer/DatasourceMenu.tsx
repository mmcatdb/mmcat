import { useEffect, useState } from 'react';
import { Spinner, Select, SelectItem } from '@nextui-org/react';
import { api } from '@/api';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { AdminerStateAction } from '@/types/adminer/Reducer';

type DatasourceMenuProps = Readonly<{
    dispatch: React.Dispatch<AdminerStateAction>;
    datasource: Datasource | undefined;
}>;

export function DatasourceMenu({ dispatch, datasource }: DatasourceMenuProps) {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | undefined>();

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);

                const response = await api.datasources.getAllDatasources({});

                if (response.status && response.data)
                    setDatasources(response.data);
                else
                    setError('Failed to load data');

            }
            catch {
                setError('Failed to load data');
            }
            finally {
                setLoading(false);
            }
        })();
    }, []);

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    return (
        <Select
            label='Datasource'
            placeholder='Select datasource'
            className='max-w-xs'
            defaultSelectedKeys={ datasource ? [ datasource.id ] : [] }
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
