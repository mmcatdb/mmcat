import { useEffect, useState } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';

export const DatasourcesTable = () => {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        const fetchDatasources = async () => {
            try {
                setLoading(true);

                const response = await api.datasources.getAllDatasources(
                    {}, // empty for urlParams + no categoryId
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

    if (error)
        return <p>{error}</p>;

    return (
        <div>
            <DatasourceTable datasources={datasources} />
        </div>
    );
};

type DatasourceTableProps = {
    datasources: Datasource[];
}

function DatasourceTable(props: DatasourceTableProps) {
    const { datasources } = props;

    return (
        <Table aria-label='Example static collection table'>
            <TableHeader>
                <TableColumn>ID</TableColumn>
                <TableColumn>Type</TableColumn>
                <TableColumn>Label</TableColumn>
                <TableColumn>Settings</TableColumn>
                <TableColumn>Configuration</TableColumn>
            </TableHeader>
            <TableBody emptyContent={'No rows to display.'}>
                {datasources.map((datasource) => (
                    <TableRow key={datasource.id}>
                        <TableCell>{datasource.id}</TableCell>
                        <TableCell>{datasource.type}</TableCell>
                        <TableCell>{datasource.label}</TableCell>
                        <TableCell>
                            {JSON.stringify(datasource.settings, null, 2)}
                        </TableCell>
                        <TableCell>
                            {JSON.stringify(datasource.configuration, null, 2)}
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

export default DatasourcesTable;
