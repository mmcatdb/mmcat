import { useState } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate, useParams } from 'react-router-dom';
import { type SortDescriptor } from '@react-types/shared';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { ErrorPage } from '@/pages/errorPages';

type DatasourcesTableProps = {
    datasources: Datasource[];
    loading: boolean;
    error: string | null;
    onDeleteDatasource: (id: string) => void;
};

export const DatasourcesTable = ({ datasources, loading, error, onDeleteDatasource }: DatasourcesTableProps) => {
    const { sortedData: sortedDatasources, sortDescriptor, setSortDescriptor } = useSortableData(datasources, {
        column: 'label',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <ErrorPage />
            </div>
        );
    }

    return (
        <div>
            <DatasourceTable
                datasources={sortedDatasources}
                onDeleteDatasource={onDeleteDatasource}
                sortDescriptor={sortDescriptor}
                onSortChange={handleSortChange}
            />
        </div>
    );
};

type DatasourceTableProps = {
  datasources: Datasource[];
  onDeleteDatasource: (id: string) => void;
  sortDescriptor: SortDescriptor;
  onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function DatasourceTable({ datasources, onDeleteDatasource, sortDescriptor, onSortChange }: DatasourceTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { categoryId } = useParams();

    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);
    const [ selectedDatasourceId, setSelectedDatasourceId ] = useState<string>();
    const navigate = useNavigate();

    const handleDeleteClick = (id: string) => {
        setSelectedDatasourceId(id);
        setModalOpen(true);
    };

    const confirmDelete = () => {
        if (selectedDatasourceId)
            onDeleteDatasource(selectedDatasourceId);

        setModalOpen(false);
    };

    const closeModal = () => {
        setSelectedDatasourceId(undefined);
        setModalOpen(false);
    };

    const handleRowAction = (key: React.Key) => {
        if (categoryId) {
            navigate(`/category/${categoryId}/datasources/${key}`, {
                state: { sortDescriptor },
            });
        }
        else {
            navigate(`/datasources/${key}`, {
                state: { sortDescriptor },
            });
        }
    };

    return (
        <div className='overflow-x-auto w-full'>
            <Table
                aria-label='Datasource Table'
                onRowAction={handleRowAction}
                sortDescriptor={sortDescriptor}
                onSortChange={onSortChange}
                removeWrapper
                isCompact
            >
                <TableHeader>
                    {/* Conditional contruct of columns before rendering */}
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
                        <TableColumn key='settings'>Settings</TableColumn>,
                        <TableColumn key='actions'>Actions</TableColumn>,
                    ]}
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {datasources.map((datasource) => (
                        <TableRow
                            key={datasource.id}
                            className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'
                        >
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{datasource.id}</TableCell> ]
                                    : []),
                                <TableCell key='label'>{datasource.label}</TableCell>,
                                <TableCell key='type'>{datasource.type}</TableCell>,
                                <TableCell key='settings'>
                                    {JSON.stringify(datasource.settings, null, 2)}
                                </TableCell>,
                                <TableCell key='actions'>
                                    <Button
                                        isIconOnly
                                        aria-label='Delete'
                                        color='danger'
                                        variant='light'
                                        onPress={() => handleDeleteClick(datasource.id)}
                                    >
                                        <TrashIcon className='w-5 h-5' />
                                    </Button>
                                </TableCell>,
                            ]}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={closeModal}
                onConfirm={confirmDelete}
                title='Confirm Deletion?'
                message='This will permanently delete the selected datasource.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </div>
    );
}
