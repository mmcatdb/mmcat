import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, type SortDescriptor } from '@heroui/react';
import { usePreferences } from '../PreferencesProvider';
import { useSortableData } from '../TableCommon';
import type { Mapping } from '@/types/mapping';
import { useCategoryInfo } from '../CategoryInfoProvider';
import { AccessPathTooltip } from './AccessPathTooltip';
import { useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { routes } from '@/routes/routes';

type MappingsTableProps = {
    mappings: Mapping[];
};

/**
 * Renders a sortable table of mappings.
 */
export function MappingsTable({ mappings }: MappingsTableProps) {
    const { sortedData: sortedMappings, sortDescriptor, setSortDescriptor } = useSortableData(mappings, {
        column: 'version',
        direction: 'descending',
    });

    return (
        <MappingsTableContent
            mappings={sortedMappings}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
        />
    );
}

type MappingsTableContentProps = {
    /** List of mappings to display. */
    mappings: Mapping[];
    /** Current sorting configuration (optional). */
    sortDescriptor?: SortDescriptor;
    /** Callback to update sorting (optional). */
    onSortChange?: (sortDescriptor: SortDescriptor) => void;
};

function MappingsTableContent({ mappings, sortDescriptor, onSortChange }: MappingsTableContentProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    // Common sorting did not work for the versions, this is override
    const sortedMappings = useMemo(() => {
        return [ ...mappings ].sort((a, b) => {
            let first: number, second: number;

            // Force version sorting for initial render
            const sortColumn = sortDescriptor?.column ?? 'version';
            const sortDirection = sortDescriptor?.direction ?? 'descending';

            if (sortColumn === 'kindName') {
                const firstStr = a.kindName.toLowerCase();
                const secondStr = b.kindName.toLowerCase();
                const cmp = firstStr.localeCompare(secondStr);
                return sortDirection === 'descending' ? -cmp : cmp;
            }

            // Default to version sorting (including initial render)
            // eslint-disable-next-line prefer-const
            first = parseFloat(a.version) || 0;
            // eslint-disable-next-line prefer-const
            second = parseFloat(b.version) || 0;

            const cmp = first < second ? -1 : (first > second ? 1 : 0);
            return sortDirection === 'descending' ? -cmp : cmp;
        });
    }, [ mappings, sortDescriptor ]);

    const navigate = useNavigate();

    const handleRowAction = (mappingId: React.Key) => {
        navigate(routes.category.mapping.resolve({
            categoryId: category.id,
            mappingId: String(mappingId),
        }));
    };

    return (
        <Table
            aria-label='Mappings Table'
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
            onRowAction={handleRowAction}
            removeWrapper
            isCompact
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id'>ID</TableColumn>,
                    ] : []),
                    <TableColumn key='kindName' allowsSorting>Kind Name</TableColumn>,
                    <TableColumn key='version' allowsSorting>Version</TableColumn>,
                    <TableColumn key='rootObjex'>Root object</TableColumn>,
                    <TableColumn key='primaryKey'>Primary Key</TableColumn>,
                    <TableColumn key='accessPath'>Access Path</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent='No mappings to display.'>
                {sortedMappings.map(mapping => (
                    <TableRow key={mapping.id} className='hover:bg-default-100 focus:bg-default-200 cursor-pointer'>
                        {[
                            ...(showTableIDs ? [
                                <TableCell key='id'>{mapping.id}</TableCell>,
                            ] : []),
                            <TableCell key='kindName'>
                                {/* The link is here for accessibility. */}
                                <Link className='underline' to={routes.category.mapping.resolve({ categoryId: category.id, mappingId: mapping.id })}>
                                    {mapping.kindName}
                                </Link>
                            </TableCell>,
                            <TableCell key='version'>
                                <span className={Number(category.systemVersionId) > Number(mapping.version) ? 'text-danger-500' : ''}>
                                    {mapping.version}
                                </span>
                            </TableCell>,
                            <TableCell key='rootObjex'>
                                {mapping.rootObjexKey.value}
                            </TableCell>,
                            <TableCell key='primaryKey'>
                                {mapping.primaryKey.signatures.join(', ')}
                            </TableCell>,
                            <TableCell key='accessPath'>
                                <AccessPathTooltip accessPath={mapping.accessPath} text='Preview' />
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
