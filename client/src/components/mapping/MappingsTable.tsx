import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@heroui/react';
import { usePreferences } from '../context/PreferencesProvider';
import { type Comparator, useSortable } from '../common/tableUtils';
import type { Mapping } from '@/types/mapping';
import { useCategoryInfo } from '../context/CategoryInfoProvider';
import { AccessPathTooltip } from './AccessPathTooltip';
import { Link, useNavigate } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { compareVersionIdsAsc } from '@/types/id';

type MappingsTableProps = {
    mappings: Mapping[];
};

/**
 * Renders a sortable table of mappings.
 */
export function MappingsTable({ mappings }: MappingsTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    const { sorted, sortDescriptor, setSortDescriptor } = useSortable(mappings, {
        column: 'version',
        direction: 'descending',
    }, mappingComparator);

    const navigate = useNavigate();

    function handleRowAction(mappingId: React.Key) {
        navigate(routes.category.mapping.resolve({
            categoryId: category.id,
            mappingId: String(mappingId),
        }));
    }

    return (
        <Table
            aria-label='Mappings Table'
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
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
            <TableBody emptyContent='No mappings to display.' items={sorted}>
                {mapping => (
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
                                {[ ...mapping.primaryKey.signatures ].join(', ')}
                            </TableCell>,
                            <TableCell key='accessPath'>
                                <AccessPathTooltip accessPath={mapping.accessPath} text='Preview' />
                            </TableCell>,
                        ]}
                    </TableRow>
                )}
            </TableBody>
        </Table>
    );
}

const mappingComparator: Comparator<Mapping> = {
    version: (a, b) => compareVersionIdsAsc(a.version, b.version),
};
