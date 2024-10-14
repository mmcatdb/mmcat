import { useState } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button, Modal, ModalHeader, ModalBody, ModalFooter, ModalContent } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate } from 'react-router-dom';

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
        navigate(`/datasources/${key}`);
    };

    return (
        <>
            <Table 
                aria-label='Datasource Table'
                onRowAction={handleRowAction}
                removeWrapper
                isCompact
            >
                <TableHeader>
                    <TableColumn>ID</TableColumn>
                    <TableColumn>Type</TableColumn>
                    <TableColumn>Label</TableColumn>
                    <TableColumn>Settings</TableColumn>
                    <TableColumn>Actions</TableColumn>
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {datasources.map((datasource) => (
                        <TableRow key={datasource.id} className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'>
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
