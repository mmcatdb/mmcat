import { useEffect, useState } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { api } from '@/api';
import type { DatasourceFromServer } from '@/types/datasource';

export const DatasourcesTable = () => {
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



// import React from 'react';
// import { Table, TableHeader, TableColumn, TableBody, TableRow, TableCell, Chip, Tooltip } from '@nextui-org/react';
// import { EditIcon } from '@/components/icons/EditIcon';
// import { DeleteIcon } from '@/components/icons/DeleteIcon';
// import { EyeIcon } from '@/components/icons/EyeIcon';
// import { columns, users } from './data';

// // TODO: Add pagination and `Add New` Button
// // https://nextui.org/docs/components/table#custom-styles

// type User = {
//   id: number;
//   label: string;
//   type: string;
//   host: string;
//   port: string;
//   database: string;
//   actions: string;
// }

// type ColumnKey = 'label' | 'id' | 'type' | 'actions';

// export function DatabasesTable() {
//     const renderCell = React.useCallback((user: User, columnKey: ColumnKey) => {
//         const cellValue = user[columnKey];

//         switch (columnKey) {
//         case 'label':
//             return (
//                 <div className='flex flex-col'>
//                     <p className='text-bold text-sm capitalize'>{cellValue}</p>
//                 </div>
//             );
//         case 'id':
//             return (
//                 <div className='flex flex-col'>
//                     <p className='text-bold text-sm capitalize'>{cellValue}</p>
//                 </div>
//             );
//         case 'type':
//             return (
//                 <Chip className='capitalize' size='sm' variant='flat'>
//                     {cellValue}
//                 </Chip>
//             );
//         case 'actions':
//             return (
//                 <div className='relative flex items-center gap-2'>
//                     <Tooltip content='Details'>
//                         <span className='text-lg text-default-400 cursor-pointer active:opacity-50'>
//                             <EyeIcon />
//                         </span>
//                     </Tooltip>
//                     <Tooltip content='Edit user'>
//                         <span className='text-lg text-default-400 cursor-pointer active:opacity-50'>
//                             <EditIcon />
//                         </span>
//                     </Tooltip>
//                     <Tooltip color='danger' content='Delete user'>
//                         <span className='text-lg text-danger cursor-pointer active:opacity-50'>
//                             <DeleteIcon />
//                         </span>
//                     </Tooltip>
//                 </div>
//             );
//         default:
//             return cellValue;
//         }
//     }, []);

//     return (
//         <Table removeWrapper aria-label='Project 2(NUMBER) details'>
//             <TableHeader columns={columns}>
//                 {(column) => (
//                     <TableColumn key={column.uid}>
//                         {column.name}
//                     </TableColumn>
//                 )}
//             </TableHeader>
//             <TableBody items={users}>
//                 {(item) => (
//                     <TableRow key={item.id}>
//                         {columns.map((column) => (
//                             <TableCell key={column.uid}>
//                                 {renderCell(item, column.uid as ColumnKey)}
//                             </TableCell>
//                         ))}
//                     </TableRow>
//                 )}
//             </TableBody>
//         </Table>
//     );
// }
