import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { usePreferences } from '../PreferencesProvider';
import { useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import type { Mapping } from '@/types/mapping';
import { useCategoryInfo } from '../CategoryInfoProvider';
import { AccessPathTooltip } from './AccessPathTooltip';
import { cn } from '@/components/utils';
import { Link } from 'react-router-dom';
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
    const { theme, showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    return (
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
                    <TableColumn key='accessPath'>
                            Access Path
                    </TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent={'No mappings to display.'}>
                {mappings.map(mapping => (
                    <TableRow
                        key={mapping.id}
                        className={cn('cursor-pointer', theme === 'dark' ? 'hover:bg-zinc-800 focus:bg-zinc-700' : 'hover:bg-zinc-100 focus:bg-zinc-200')}
                    >
                        {[
                            ...(showTableIDs
                                ? [ <TableCell key='id'>{mapping.id}</TableCell> ]
                                : []),
                            <TableCell key='kindName'>
                                <Link to={routes.category.mapping.resolve({ categoryId: category.id, mappingId: mapping.id })}>
                                    {mapping.kindName}
                                </Link>
                            </TableCell>,
                            <TableCell key='version'>
                                <span className={Number(category.systemVersionId) > Number(mapping.version) ? 'text-red-500' : ''}>
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
