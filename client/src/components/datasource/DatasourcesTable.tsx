import { useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@heroui/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/Datasource';
import { useNavigate, useParams } from 'react-router-dom';
import { usePreferences } from '../context/PreferencesProvider';
import { ConfirmationModal } from '../common/tableComponents';
import { useSortable } from '../common/tableUtils';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';
import { type Id } from '@/types/id';
import { useCached } from '../hooks/useCached';
import { api } from '@/api';

type DatasourcesTableProps = {
    /** List of datasources to display. */
    datasources: Datasource[];
    /** Callback emited when a datasource is deleted. */
    onDelete: (id: Id) => void;
    /** IDs of datasources with active mappings (if some exist). */
    datasourcesWithMappingsIds?: Id[];
};

/**
 * Renders a sortable table of datasources with delete functionality.
 */
export function DatasourcesTable({ datasources, onDelete, datasourcesWithMappingsIds = [] }: DatasourcesTableProps) {
    const [ toDelete, setToDelete ] = useState<Datasource>();
    const { showTableIDs } = usePreferences().preferences;
    const { categoryId } = useParams();

    // Manage sorting state for the table
    const { sorted, sortDescriptor, setSortDescriptor } = useSortable(datasources, {
        column: 'label',
        direction: 'ascending',
    });

    const navigate = useNavigate();

    function navigateToDatasource(key: React.Key) {
        const datasourceId = key as Id;
        const path = categoryId
            ? routes.category.datasources.detail.resolve({ categoryId, datasourceId })
            : routes.datasources.detail.resolve({ datasourceId });
        navigate(path, { state: { sortDescriptor } });
    }

    return (<>
        <DeleteDatasourceModal
            datasource={toDelete}
            onClose={() => setToDelete(undefined)}
            onDelete={onDelete}
        />

        <Table
            aria-label='Datasource Table'
            onRowAction={navigateToDatasource}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id' allowsSorting>ID</TableColumn>,
                    ] : []),
                    <TableColumn key='label' allowsSorting>Label</TableColumn>,
                    <TableColumn key='type' allowsSorting>Type</TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>

            <TableBody emptyContent='No rows to display.' items={sorted}>
                {datasource => {
                    const hasMappings = datasourcesWithMappingsIds.includes(datasource.id);

                    return (
                        <TableRow key={datasource.id} className='cursor-pointer hover:bg-default-100 focus:bg-default-200'>
                            {[
                                ...(showTableIDs ? [
                                    <TableCell key='id'>{datasource.id}</TableCell>,
                                ] : []),
                                <TableCell key='label'>{datasource.label}</TableCell>,
                                <TableCell key='type'>{datasource.type}</TableCell>,
                                <TableCell key='actions' title='Delete datasource'>
                                    <Button
                                        isIconOnly
                                        aria-label='Delete'
                                        title='Delete datasource'
                                        color='danger'
                                        variant='light'
                                        onPress={() => setToDelete(datasource)}
                                        disabled={hasMappings}
                                    >
                                        <TrashIcon className='size-5' />
                                    </Button>
                                </TableCell>,
                            ]}
                        </TableRow>
                    );
                }}
            </TableBody>
        </Table>
    </>);
}

type DeleteDatasourceModalProps = {
    datasource: Datasource | undefined;
    onClose: () => void;
    onDelete: (id: Id) => void;
};

function DeleteDatasourceModal({ datasource, onClose, onDelete }: DeleteDatasourceModalProps) {
    const [ isFetching, setIsFetching ] = useState<boolean>(false);

    const cached = useCached(datasource);

    async function confirmDelete() {
        if (!datasource)
            return;

        setIsFetching(true);
        const response = await api.datasources.deleteDatasource({ id: datasource.id });
        setIsFetching(false);
        if (!response.status) {
            toast.error('Failed to delete datasource.');
            return;
        }

        toast.success(`Datasource ${datasource.label} deleted successfully!`);
        onDelete(datasource.id);
        onClose();
    }

    return (
        <ConfirmationModal
            isOpen={!!datasource}
            onClose={onClose}
            onConfirm={confirmDelete}
            isFetching={isFetching}
            title='Confirm Deletion?'
            message={`This will permanently delete the datasource ${cached?.label ?? ''}.`}
            confirmButtonText='Yes, Delete'
            cancelButtonText='Cancel'
            confirmButtonColor='danger'
        />
    );
}
