import { useEffect, useState } from 'react';
import { api } from '@/api';
import { ButtonGroupComponent } from '@/components/adminer/ButtonGroupComponent';
import { Spinner, ButtonGroup, Button, Spacer } from '@nextui-org/react';
import { Datasource, DatasourceType } from '@/types/datasource';

export const DatabaseMenu: React.FC = () => {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);
    const [ database, setDatabase] = useState<DatasourceType>(DatasourceType.postgresql);

    const handleClick = (db: DatasourceType) => {
        setDatabase(db);
    };

    useEffect(() => {
        const fetchDatasources = async () => {
            try {
                setLoading(true);

                const response = await api.datasources.getAllDatasources(
                    {},
                );
    
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
        };
    
        fetchDatasources();
    }, []);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error) {
        return <p style={{ fontSize: '14px' }}>{error}</p>;
    }

    return (
        <div style={{ fontSize: '14px' }}>
            <ButtonGroup>
                <Button onPress={() => handleClick(DatasourceType.postgresql)} color={database === DatasourceType.postgresql ? "primary" : "default"}>
                    PostgreSQL
                </Button>
                <Button onPress={() => handleClick(DatasourceType.mongodb)} color={database === DatasourceType.mongodb ? "primary" : "default"}>
                    MongoDB
                </Button>
                <Button onPress={() => handleClick(DatasourceType.neo4j)} color={database === DatasourceType.neo4j ? "primary" : "default"}>
                    Neo4j
                </Button>
            </ButtonGroup>

            <Spacer y={2} />

            <ButtonGroupComponent db={database} data={datasources}/>
        </div>
    );
};
