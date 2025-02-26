import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { usePreferences } from '../PreferencesProvider';
import { useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import type { Mapping } from '@/types/mapping';
import { useCategoryInfo } from '../CategoryInfoProvider';
import { AccessPathTooltip } from './AccessPathTooltip';
import { useNavigate } from 'react-router-dom';
import { routes } from '@/routes/routes';

type MappingsTableProps = {
    mappings: Mapping[];
};

export function MappingsTable({ mappings }: MappingsTableProps) {
    const { sortedData: sortedMappings, sortDescriptor, setSortDescriptor } = useSortableData(mappings, {
        column: 'kindName',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    return (
        <MappingsTableContent
            mappings={sortedMappings}
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
        />
    );
}

type MappingsTableContentProps = {
    mappings: Mapping[];
    sortDescriptor: SortDescriptor;
    onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function MappingsTableContent({ mappings, sortDescriptor, onSortChange }: MappingsTableContentProps) {
    const { showTableIDs } = usePreferences().preferences;
    const navigate = useNavigate();
    const { category } = useCategoryInfo();

    const handleRowAction = (mappingId: React.Key) => {
        navigate(routes.category.mapping.resolve({ categoryId: category.id, mappingId: String(mappingId) }));
    };

    return (
        <Table
            aria-label='Mappings Table'
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
            onRowAction={(key: React.Key) => handleRowAction(key)}
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
                    <TableColumn key='accessPath'>
                            Access Path
                    </TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent={'No mappings to display.'}>
                {mappings.map(mapping => (
                    <TableRow
                        key={mapping.id}
                        className='cursor-pointer hover:bg-default-100 focus:bg-default-200'
                    >
                        {[
                            ...(showTableIDs
                                ? [ <TableCell key='id'>{mapping.id}</TableCell> ]
                                : []),
                            <TableCell key='kindName'>
                                {mapping.kindName}
                            </TableCell>,
                            <TableCell key='version'>
                                <span className={Number(category.systemVersionId) > Number(mapping.version) ? 'text-danger-500' : ''}>
                                    {mapping.version}
                                </span>
                            </TableCell>,
                            <TableCell key='rootObject'>
                                {/* // TODO - load whole schema category and display the object name that corresponds to this key */}
                                {mapping.rootObjexKey.value}
                            </TableCell>,
                            <TableCell key='primaryKey'>
                                {mapping.primaryKey.signatures.join(', ')}
                            </TableCell>,
                            <TableCell key='accessPath'>
                                <AccessPathTooltip accessPath={mapping.accessPath} />
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
