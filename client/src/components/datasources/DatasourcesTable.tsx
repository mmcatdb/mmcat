import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import type { Datasource } from '@/types/datasource';

type DatasourcesTableProps = {
    datasources: Datasource[];
    loading: boolean;
    error: string | null;
};

export const DatasourcesTable = ({ datasources, loading, error }: DatasourcesTableProps) => {
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
        <Table aria-label='Datasource Table'>
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
