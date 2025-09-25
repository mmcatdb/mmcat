import { useState, useMemo } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button, type SortDescriptor } from '@heroui/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/Datasource';
import { useNavigate, useParams } from 'react-router-dom';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';

type DatasourcesTableProps = {
    /** List of datasources to display. */
    datasources: Datasource[];
    /** Callback to handle datasource deletion. */
    deleteDatasource: (id: string) => void;
    /** IDs of datasources with active mappings (if some exist). */
    datasourcesWithMappings?: string[];
};

/**
 * Renders a sortable table of datasources with delete functionality.
 */
export function DatasourcesTable({ datasources, deleteDatasource, datasourcesWithMappings = [] }: DatasourcesTableProps) {
    // Manage sorting state for the table
    const { sortedData: sortedDatasources, sortDescriptor, setSortDescriptor } = useSortableData(datasources, {
        column: 'label',
        direction: 'ascending',
    });

    return (
        <SortedDatasourcesTable
            datasources={sortedDatasources}
            deleteDatasource={deleteDatasource}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
            datasourcesWithMappings={datasourcesWithMappings}
        />
    );
}

/**
 * Hook to manage datasource selection and deletion modal state.
 *
 * @param datasources - List of datasources.
 * @param deleteDatasource - Callback to handle deletion.
 * @returns State and functions for managing deletion.
 */
function useDatasourceSelection(datasources: Datasource[], deleteDatasource: (id: string) => void) {
    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);
    const [ selectedDatasourceId, setSelectedDatasourceId ] = useState<string>();
    const [ isDeleting, setIsDeleting ] = useState<boolean>(false);

    // Memoize the selected datasource to avoid unnecessary lookups
    const selectedDatasource = useMemo(
        () => datasources.find(d => d.id === selectedDatasourceId),
        [ datasources, selectedDatasourceId ],
    );

    /**
     * Initiates deletion by opening the confirmation modal.
     */
    function handleDeleteClick(id: string) {
        setSelectedDatasourceId(id);
        setModalOpen(true);
    }

    /**
     * Confirms deletion and notifies the user.
     */
    function confirmDelete() {
        if (selectedDatasourceId && selectedDatasource) {
            setIsDeleting(true);
            try {
                deleteDatasource(selectedDatasourceId);
                toast.success(`Datasource ${selectedDatasource.label} deleted successfully!`);
            }
            catch {
                toast.error(`Failed to delete datasource ${selectedDatasource.label}.`);
            }
            finally {
                setIsDeleting(false);
            }
        }
        setModalOpen(false);
        setSelectedDatasourceId(undefined);
    }

    /**
     * Closes the modal and clears selection.
     */
    function closeModal() {
        setSelectedDatasourceId(undefined);
        setModalOpen(false);
    }

    return {
        isModalOpen,
        selectedDatasource,
        isDeleting,
        handleDeleteClick,
        confirmDelete,
        closeModal,
    };
}

type ConfirmationModalWrapperProps = {
    isOpen: boolean;
    /** The datasource to delete, if selected. */
    datasource?: Datasource;
    /** Callback to confirm deletion. */
    onConfirm: () => void;
    /** Callback to close the modal. */
    onClose: () => void;
    /** Whether deletion is in progress. */
    isDeleting: boolean;
};

/**
 * Reusable component to render the deletion confirmation modal.
 *
 * @returns A React component rendering the confirmation modal.
 */
function ConfirmationModalWrapper({ isOpen, datasource, onConfirm, onClose, isDeleting }: ConfirmationModalWrapperProps) {
    return (
        <ConfirmationModal
            isOpen={isOpen}
            onClose={onClose}
            onConfirm={onConfirm}
            isFetching={isDeleting}
            title='Confirm Deletion?'
            message={`This will permanently delete the datasource ${datasource?.label ?? ''}.`}
            confirmButtonText='Yes, Delete'
            cancelButtonText='Cancel'
            confirmButtonColor='danger'
        />
    );
}

type SortedDatasourcesTableProps = {
    /** Sorted list of datasources. */
    datasources: Datasource[];
    /** Callback to handle datasource deletion. */
    deleteDatasource: (id: string) => void;
    /** Current sorting configuration. */
    sortDescriptor: SortDescriptor;
    /** Callback to update sorting. */
    onSortChange: (sortDescriptor: SortDescriptor) => void;
    /** IDs of datasources with active mappings (if some exist). */
    datasourcesWithMappings?: string[];
};

/**
 * Renders the table of datasources with sorting and deletion capabilities.
 */
function SortedDatasourcesTable({ datasources, deleteDatasource, sortDescriptor, onSortChange, datasourcesWithMappings = [] }: SortedDatasourcesTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { categoryId } = useParams();
    const navigate = useNavigate();

    const { isModalOpen, selectedDatasource, isDeleting, handleDeleteClick, confirmDelete, closeModal } = useDatasourceSelection(datasources, deleteDatasource);

    /**
     * Navigates to the datasource’s detail page when a row is clicked.
     */
    function navigateToDatasource(key: React.Key) {
        const datasourceId = key as string;
        const path = categoryId
            ? routes.category.datasources.detail.resolve({ categoryId, datasourceId })
            : routes.datasources.detail.resolve({ datasourceId });
        navigate(path, { state: { sortDescriptor } });
    }

    return (<>
        <Table
            aria-label='Datasource Table'
            onRowAction={navigateToDatasource}
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
        >
            <TableHeader>
                {[
                    ...(showTableIDs
                        ? [
                            <TableColumn key='id' allowsSorting>ID</TableColumn>,
                        ]
                        : []),
                    <TableColumn key='label' allowsSorting>Label</TableColumn>,
                    <TableColumn key='type' allowsSorting>Type</TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>

            <TableBody emptyContent='No rows to display.'>
                {datasources.map(datasource => {
                    const hasMappings = datasourcesWithMappings.includes(datasource.id);

                    return (
                        <TableRow key={datasource.id} className='cursor-pointer hover:bg-default-100 focus:bg-default-200'>
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{datasource.id}</TableCell> ]
                                    : []),
                                <TableCell key='label'>{datasource.label}</TableCell>,
                                <TableCell key='type'>{datasource.type}</TableCell>,
                                <TableCell key='actions' title='Delete datasource'>
                                    <Button
                                        isIconOnly
                                        aria-label='Delete'
                                        color='danger'
                                        variant='light'
                                        onPress={() => {
                                            if (!hasMappings)
                                                handleDeleteClick(datasource.id);
                                        }}
                                        title='Delete datasource'
                                        disabled={hasMappings}
                                    >
                                        <TrashIcon className='size-5' />
                                    </Button>
                                </TableCell>,
                            ]}
                        </TableRow>
                    );
                })}
            </TableBody>
        </Table>
        <ConfirmationModalWrapper
            isOpen={isModalOpen}
            datasource={selectedDatasource}
            onConfirm={confirmDelete}
            onClose={closeModal}
            isDeleting={isDeleting}
        />
    </>);
}
