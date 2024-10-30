import { useEffect, useState } from 'react';
import { api } from '@/api';
import { Spinner, Button } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';
import { type AdminerStateAction } from '@/types/adminer/Reducer';

type DatasourceMenuProps = Readonly<{
    datasource: Datasource | undefined;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function DatasourceMenu({ datasource, dispatch }: DatasourceMenuProps) {
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
            catch (err) {
                setError('Failed to load data');
            }
            finally {
                setLoading(false);
            }
        })();
    }, []);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    return (
        <div className='flex flex-wrap gap-3 items-center'>
            {datasources
                .filter((item) =>
                    item.type === DatasourceType.postgresql ||
                    item.type === DatasourceType.mongodb ||
                    item.type === DatasourceType.neo4j,
                )
                .map((item) => (
                    <Button
                        key={item.id}
                        onPress={() => dispatch({ type:'datasource', newDatasource: item })}
                        color={datasource === item ? 'primary' : 'default'}
                        className='flex-1 min-w-[150px] max-w-[200px]'
                    >
                        <span className='truncate'>{item.label}</span>
                    </Button>
                ))}
        </div>
    );
}
