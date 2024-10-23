import { useState } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button, Modal, ModalHeader, ModalBody, ModalFooter, ModalContent } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate } from 'react-router-dom';
import { type SortDescriptor } from '@react-types/shared';

type DatasourcesTableProps = {
    datasources: Datasource[];
    loading: boolean;
    error: string | null;
    onDeleteDatasource: (id: string) => void;
};

export const DatasourcesTable = ({ datasources, loading, error, onDeleteDatasource }: DatasourcesTableProps) => {
    const [ sortDescriptor, setSortDescriptor ] = useState<SortDescriptor>({
        column: 'id',
        direction: 'ascending',
    });

    const sortedDatasources = [ ...datasources ].sort((a, b) => {
        let fieldA = a[sortDescriptor.column as keyof Datasource];
        let fieldB = b[sortDescriptor.column as keyof Datasource];

        if (typeof fieldA === 'string' && typeof fieldB === 'string') {
            fieldA = fieldA.toLowerCase();
            fieldB = fieldB.toLowerCase();
        }

        if (fieldA < fieldB)
            return sortDescriptor.direction === 'ascending' ? -1 : 1;
        if (fieldA > fieldB)
            return sortDescriptor.direction === 'ascending' ? 1 : -1;
        return 0;
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

    // TODO: error page
    if (error)
        return <p>{error}</p>;

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
    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);
    const [ selectedDatasourceId, setSelectedDatasourceId ] = useState<string | null>(null);
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
        setSelectedDatasourceId(null);
        setModalOpen(false);
    };

    const handleRowAction = (key: React.Key) => {
        navigate(`/datasources/${key}`, {
            state: { sortDescriptor },
        });
    };

    return (
        <>
            <Table
                aria-label='Datasource Table'
                onRowAction={handleRowAction}
                sortDescriptor={sortDescriptor}
                onSortChange={onSortChange}
                removeWrapper
                isCompact
            >
                <TableHeader>
                    <TableColumn key='id' allowsSorting>
                        ID
                    </TableColumn>
                    <TableColumn key='type' allowsSorting>
                        Type
                    </TableColumn>
                    <TableColumn key='label' allowsSorting>
                        Label
                    </TableColumn>
                    <TableColumn>Settings</TableColumn>
                    <TableColumn>Actions</TableColumn>
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {datasources.map((datasource) => (
                        <TableRow
                            key={datasource.id}
                            className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'
                        >
                            <TableCell>{datasource.id}</TableCell>
                            <TableCell>{datasource.type}</TableCell>
                            <TableCell>{datasource.label}</TableCell>
                            <TableCell>
                                {JSON.stringify(datasource.settings, null, 2)}
                            </TableCell>
                            <TableCell>
                                <Button
                                    isIconOnly
                                    aria-label='Delete'
                                    color='danger'
                                    variant='light'
                                    onPress={() => handleDeleteClick(datasource.id)}
                                >
                                    <TrashIcon className='w-5 h-5' />
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            {/* Confirmation of datasource delete */}
            <Modal
                isOpen={isModalOpen}
                onClose={closeModal}
            >
                <ModalContent>
                    <ModalHeader>
                        <p>Confirm Deletion?</p>
                    </ModalHeader>
                    <ModalBody>
                        <p>This will permanently delete the selected datasource.</p>
                    </ModalBody>
                    <ModalFooter>
                        <Button onPress={closeModal}>
                            Cancel
                        </Button>
                        <Button color='danger' onPress={confirmDelete}>
                            Yes, Delete
                        </Button>
                    </ModalFooter>
                </ModalContent>
            </Modal>
        </>
    );
}

export default DatasourcesTable;
