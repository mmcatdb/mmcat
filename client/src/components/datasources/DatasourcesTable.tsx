import { useState, useMemo } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate, useParams } from 'react-router-dom';
import { type SortDescriptor } from '@react-types/shared';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';

/**
 * Props for the DatasourcesTable component.
 *
 * @interface DatasourcesTableProps
 * @property datasources - List of datasources to display.
 * @property deleteDatasource - Callback to handle datasource deletion.
 * @property datasourcesWithMappings - IDs of datasources with active mappings (optional).
 */
type DatasourcesTableProps = {
    datasources: Datasource[];
    deleteDatasource: (id: string) => void;
    datasourcesWithMappings?: string[];
};

/**
 * Props for the DatasourceTable component.
 *
 * @interface DatasourceTableProps
 * @property datasources - Sorted list of datasources.
 * @property deleteDatasource - Callback to handle datasource deletion.
 * @property sortDescriptor - Current sorting configuration.
 * @property onSortChange - Callback to update sorting.
 * @property datasourcesWithMappings - IDs of datasources with active mappings (optional).
 */
type DatasourceTableProps = {
    datasources: Datasource[];
    deleteDatasource: (id: string) => void;
    sortDescriptor: SortDescriptor;
    onSortChange: (sortDescriptor: SortDescriptor) => void;
    datasourcesWithMappings?: string[];
};

/**
 * Props for the ConfirmationModalWrapper component.
 *
 * @interface ConfirmationModalWrapperProps
 * @property isOpen - Whether the modal is visible.
 * @property datasource - The datasource to delete, if selected.
 * @property onConfirm - Callback to confirm deletion.
 * @property onClose - Callback to close the modal.
 * @property isDeleting - Whether deletion is in progress.
 */
type ConfirmationModalWrapperProps = {
    isOpen: boolean;
    datasource?: Datasource;
    onConfirm: () => void;
    onClose: () => void;
    isDeleting: boolean;
};

/**
 * Renders a sortable table of datasources with delete functionality.
 *
 * @param props - The component props.
 * @returns A React component rendering the datasources table.
 */
export function DatasourcesTable({ datasources, deleteDatasource, datasourcesWithMappings = [] }: DatasourcesTableProps) {
    // Manage sorting state for the table
    const { sortedData: sortedDatasources, sortDescriptor, setSortDescriptor } = useSortableData(datasources, {
        column: 'label',
        direction: 'ascending',
    });

    /**
     * Updates the sort descriptor when the user changes sorting.
     */
    function handleSortChange(newSortDescriptor: SortDescriptor) {
        setSortDescriptor(newSortDescriptor);
    }

    return (
        <DatasourceTable
            datasources={sortedDatasources}
            deleteDatasource={deleteDatasource}
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
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
    const handleDeleteClick = (id: string) => {
        setSelectedDatasourceId(id);
        setModalOpen(true);
    };

    /**
     * Confirms deletion and notifies the user.
     */
    const confirmDelete = () => {
        if (selectedDatasourceId && selectedDatasource) {
            setIsDeleting(true);
            try {
                deleteDatasource(selectedDatasourceId);
                toast.success(`Datasource ${selectedDatasource.label} deleted successfully!`);
            }
            catch (error) {
                toast.error(`Failed to delete datasource ${selectedDatasource.label}.`);
            }
            finally {
                setIsDeleting(false);
            }
        }
        setModalOpen(false);
        setSelectedDatasourceId(undefined);
    };

    /**
     * Closes the modal and clears selection.
     */
    const closeModal = () => {
        setSelectedDatasourceId(undefined);
        setModalOpen(false);
    };

    return {
        isModalOpen,
        selectedDatasource,
        isDeleting,
        handleDeleteClick,
        confirmDelete,
        closeModal,
    };
}

/**
 * Navigates to a datasource’s detail page, preserving sort state.
 *
 * @param navigate - The navigate function from react-router-dom.
 * @param categoryId - The optional category ID for contextual navigation.
 * @param datasourceId - The ID of the datasource to navigate to.
 * @param sortDescriptor - The current sorting configuration.
 */
function navigateToDatasource(
    navigate: (path: string, options?: { state?: unknown }) => void,
    categoryId: string | undefined,
    datasourceId: React.Key,
    sortDescriptor: SortDescriptor,
) {
    const path = categoryId
        ? routes.category.datasources.resolve({ categoryId }) + `/${datasourceId}`
        : `/datasources/${datasourceId}`;
    navigate(path, { state: { sortDescriptor } });
}

/**
 * Reusable component to render the deletion confirmation modal.
 *
 * @param props - The modal component props.
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

/**
 * Renders the table of datasources with sorting and deletion capabilities.
 *
 * @param props - The component props.
 * @returns A React component rendering the datasource table.
 */
function DatasourceTable({
    datasources,
    deleteDatasource,
    sortDescriptor,
    onSortChange,
    datasourcesWithMappings = [],
}: DatasourceTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { categoryId } = useParams();
    const navigate = useNavigate();
    const { isModalOpen, selectedDatasource, isDeleting, handleDeleteClick, confirmDelete, closeModal } =
        useDatasourceSelection(datasources, deleteDatasource);

    /**
     * Navigates to the datasource’s detail page when a row is clicked.
     */
    function handleRowAction(key: React.Key) {
        navigateToDatasource(navigate, categoryId, key, sortDescriptor);
    }

    return (<>
        <Table
            aria-label='Datasource Table'
            onRowAction={handleRowAction}
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
        >
            <TableHeader>
                {[
                    ...(showTableIDs
                        ? [
                            <TableColumn key='id' allowsSorting>
                                      ID
                            </TableColumn>,
                        ]
                        : []),
                    <TableColumn key='label' allowsSorting>
                            Label
                    </TableColumn>,
                    <TableColumn key='type' allowsSorting>
                            Type
                    </TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent='No rows to display.'>
                {datasources.map(datasource => {
                    const hasMappings = datasourcesWithMappings.includes(datasource.id);
                    return (
                        <TableRow
                            key={datasource.id}
                            className='cursor-pointer hover:bg-default-100 focus:bg-default-200'
                        >
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{datasource.id}</TableCell> ]
                                    : []),
                                <TableCell key='label'>{datasource.label}</TableCell>,
                                <TableCell key='type'>{datasource.type}</TableCell>,
                                <TableCell
                                    key='actions'
                                    title={
                                        hasMappings
                                            ? 'Delete disabled - datasource has active mappings'
                                            : 'Delete datasource'
                                    }
                                >
                                    <Button
                                        isIconOnly
                                        aria-label='Delete'
                                        color='danger'
                                        variant='light'
                                        onPress={() => handleDeleteClick(datasource.id)}
                                        isDisabled={hasMappings}
                                    >
                                        <TrashIcon className='w-5 h-5' />
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
