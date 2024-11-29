import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { usePreferences } from '../PreferencesProvider';
import { useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import type { Mapping } from '@/types/mapping';
import { useCategoryInfo } from '../CategoryInfoProvider';

type MappingsTableProps = {
    mappings: Mapping[];
    loading: boolean;
    error: string | undefined;
};

export function MappingsTable({ mappings, loading, error }: MappingsTableProps) {
    const { sortedData: sortedMappings, sortDescriptor, setSortDescriptor } = useSortableData(mappings, {
        column: 'kindName',
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

    // TODO: Add a custom error component
    if (error) 
        return <p>{error}</p>;

    return (
        <div>
            <MappingsTableContent
                mappings={sortedMappings}
                sortDescriptor={sortDescriptor}
                onSortChange={handleSortChange}
            />
        </div>
    );
}

type MappingsTableContentProps = {
    mappings: Mapping[];
    sortDescriptor: SortDescriptor;
    onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function MappingsTableContent({ mappings, sortDescriptor, onSortChange }: MappingsTableContentProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    return (
        <div className='overflow-x-auto w-full'>
            <Table
                aria-label='Mappings Table'
                sortDescriptor={sortDescriptor}
                onSortChange={onSortChange}
                removeWrapper
                isCompact
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
                        <TableColumn key='kindName' allowsSorting>
                            Kind Name
                        </TableColumn>,
                        <TableColumn key='version' allowsSorting>
                            Version
                        </TableColumn>,
                        <TableColumn key='rootObject'>
                            Root object
                        </TableColumn>,
                        <TableColumn key='primaryKey'>
                            Primary Key
                        </TableColumn>,
                    ]}
                </TableHeader>
                <TableBody emptyContent={'No mappings to display.'}>
                    {mappings.map((mapping) => (
                        <TableRow
                            key={mapping.id}
                            className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'
                        >
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{mapping.id}</TableCell> ]
                                    : []),
                                <TableCell key='kindName'>{mapping.kindName}</TableCell>,
                                <TableCell key='version'>
                                    <span className={Number(category.systemVersionId) > Number(mapping.version) ? 'text-red-500' : ''}>
                                        {mapping.version}
                                    </span>
                                </TableCell>,
                                <TableCell key='rootObject'>
                                    {mapping.rootObjectKey.value}
                                </TableCell>,
                                <TableCell key='primaryKey'>
                                    {mapping.primaryKey.signatures.join(', ')}
                                </TableCell>,
                            ]}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
}
