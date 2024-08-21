import { useEffect, useState } from 'react';
import { api } from '@/api';
import type { DatasourceFromServer } from '@/types/datasource';
import { Spinner } from '@nextui-org/react';
import {
    Table,
    TableHeader,
    TableBody,
    TableColumn,
    TableRow,
    TableCell,
} from '@nextui-org/table';

export const DatabasesTable = () => {
    const [ datasources, setDatasources ] = useState<DatasourceFromServer[]>([]);
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
            } catch (err) {
                setError('Failed to load data');
            } finally {
                setLoading(false);
            }
        };
    
        fetchDatasources();
    }, []);
    

    if (loading) {
        return (
            <div>
                <Spinner/>
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

interface DatasourceTableProps {
    datasources: DatasourceFromServer[];
}

const DatasourceTable: React.FC<DatasourceTableProps> = ({ datasources }) => {
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
};
