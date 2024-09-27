import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import type { Datasource } from '@/types/datasource';
import { TrashIcon } from '@heroicons/react/24/outline';

type DatasourcesTableProps = {
    datasources: Datasource[];
    loading: boolean;
    error: string | null;
    onDeleteDatasource: (id: string) => void;
};

export const DatasourcesTable = ({ datasources, loading, error, onDeleteDatasource }: DatasourcesTableProps) => {
    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    // TODO: error page
    if (error) 
        return <p>{error}</p>;

    return (
        <div>
            <DatasourceTable datasources={datasources} onDeleteDatasource={onDeleteDatasource} />
        </div>
    );
};

type DatasourceTableProps = {
    datasources: Datasource[];
    onDeleteDatasource: (id: string) => void;
}

function DatasourceTable({ datasources, onDeleteDatasource }: DatasourceTableProps) {
    return (
        <Table aria-label='Datasource Table'>
            <TableHeader>
                <TableColumn>ID</TableColumn>
                <TableColumn>Type</TableColumn>
                <TableColumn>Label</TableColumn>
                <TableColumn>Settings</TableColumn>
                <TableColumn>Configuration</TableColumn>
                <TableColumn>Actions</TableColumn>
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
                        <TableCell>
                            <Button
                                isIconOnly
                                aria-label='Delete'
                                color='danger'
                                variant='light'
                                onPress={() => onDeleteDatasource(datasource.id)}
                            >
                                <TrashIcon className='w-5 h-5' />
                            </Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

export default DatasourcesTable;
