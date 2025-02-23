import { useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate, useParams } from 'react-router-dom';
import { type SortDescriptor } from '@react-types/shared';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { cn } from '@/components/utils';

type DatasourcesTableProps = {
    datasources: Datasource[];
    deleteDatasource: (id: string) => void;
};

export function DatasourcesTable({ datasources, deleteDatasource }: DatasourcesTableProps) {
    const { sortedData: sortedDatasources, sortDescriptor, setSortDescriptor } = useSortableData(datasources, {
        column: 'label',
        direction: 'ascending',
    });

    function handleSortChange(newSortDescriptor: SortDescriptor) {
        setSortDescriptor(newSortDescriptor);
    }

    return (
        <DatasourceTable
            datasources={sortedDatasources}
            deleteDatasource={deleteDatasource}
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
        />
    );
}

type DatasourceTableProps = {
  datasources: Datasource[];
  deleteDatasource: (id: string) => void;
  sortDescriptor: SortDescriptor;
  onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function DatasourceTable({ datasources, deleteDatasource, sortDescriptor, onSortChange }: DatasourceTableProps) {
    const { theme, showTableIDs } = usePreferences().preferences;
    const { categoryId } = useParams();

    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);
    const [ selectedDatasourceId, setSelectedDatasourceId ] = useState<string>();
    const navigate = useNavigate();

    function handleDeleteClick(id: string) {
        setSelectedDatasourceId(id);
        setModalOpen(true);
    }

    function confirmDelete() {
        if (selectedDatasourceId)
            deleteDatasource(selectedDatasourceId);

        setModalOpen(false);
    }

    function closeModal() {
        setSelectedDatasourceId(undefined);
        setModalOpen(false);
    }

    function handleRowAction(key: React.Key) {
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
    }

    return (<>
        <Table
            aria-label='Datasource Table'
            onRowAction={handleRowAction}
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
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
                    // <TableColumn key='settings'>Settings</TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent={'No rows to display.'}>
                {datasources.map(datasource => (
                    <TableRow
                        key={datasource.id}
                        className={cn('cursor-pointer',
                            theme === 'dark' ? 'hover:bg-zinc-800 focus:bg-zinc-700' : 'hover:bg-zinc-100 focus:bg-zinc-200')}
                    >
                        {[
                            ...(showTableIDs
                                ? [ <TableCell key='id'>{datasource.id}</TableCell> ]
                                : []),
                            <TableCell key='label'>{datasource.label}</TableCell>,
                            <TableCell key='type'>{datasource.type}</TableCell>,
                            // <TableCell key='settings' className='break-all'>
                            //     {JSON.stringify(datasource.settings, null, 2)}
                            // </TableCell>,
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
    </>);
}
