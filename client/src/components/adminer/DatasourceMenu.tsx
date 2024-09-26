import { useEffect, useState } from 'react';
import { api } from '@/api';
import { Spinner, ButtonGroup, Button } from '@nextui-org/react';
import { type Datasource, DatasourceType } from '@/types/datasource';

type DatasourceMenuProps = Readonly<{
    datasource: Datasource | undefined;
    setDatasource: (datasource: Datasource) => void;
}>;

export function DatasourceMenu({ datasource, setDatasource }: DatasourceMenuProps) {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

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
        <div>
            <ButtonGroup
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    width: '100%',
                }}
            >
                {datasources
                    .filter((item) => item.type === DatasourceType.postgresql || item.type === DatasourceType.mongodb || item.type === DatasourceType.neo4j)
                    .map((item) => (
                        <Button
                            key={item.id}
                            onPress={() => setDatasource(item)}
                            color={datasource === item ? 'primary' : 'default'}
                            style={{
                                flex: 1,
                                minWidth: '50px',
                            }}
                        >
                            {item.label}
                        </Button>
                    ))}
            </ButtonGroup>
        </div>
    );
}
