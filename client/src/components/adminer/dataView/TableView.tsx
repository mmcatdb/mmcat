import { useMemo } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@heroui/react';
import { DocumentDisplay } from '@/components/adminer/dataView/DocumentDisplay';
import { getTableFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/Datasource';
import { type TableResponse, type GraphResponse, View } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type TableViewProps = {
    /** The data to display. */
    data: TableResponse | GraphResponse;
    /** References from and to the current kind. */
    kindReferences: KindReference[];
    /** Name of the current kind. */
    kind: string;
    /** The id of selected datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
};

/**
 * Component for displaying data in table
 */
export function TableView({ data, kindReferences, kind, datasourceId, datasources }: TableViewProps ) {
    const { tableData, columnNames } = useMemo(() => {
        if (data.type === View.graph)
            return getTableFromGraphData(data);

        return { tableData: data, columnNames: data.metadata.propertyNames };
    }, [ data ]);

    if (tableData === undefined || tableData.data.length === 0 || tableData.metadata.itemCount === 0)
        return <p>No rows to display.</p>;

    return (<>
        {tableData && columnNames && (
            <Table isStriped isCompact aria-label='Table'>
                <TableHeader>
                    {columnNames.map((columnName, index) => (
                        <TableColumn key={index}>{columnName}</TableColumn>
                    ))}
                </TableHeader>

                {/* This is intentional. Yes, it's just disgusting. Thanks, heroui, for being such colossal assholes. */}
                {TableViewBody({
                    tableBodyData: tableData.data,
                    columnNames: columnNames,
                    kindReferences: kindReferences,
                    kind: kind,
                    datasourceId: datasourceId,
                    datasources: datasources,
                })}
            </Table>
        )}
    </>);
}

type TableViewBodyProps = {
    tableBodyData: string[][];
    columnNames: string[];
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
};

function TableViewBody({ tableBodyData, columnNames, kindReferences, kind, datasourceId, datasources }: TableViewBodyProps ) {
    return (
        <TableBody emptyContent='No rows to display.'>
            {tableBodyData.map((row, rowIndex) => (
                <TableRow key={rowIndex}>
                    {row.map((cellItem, cellIndex) => (
                        <TableCell key={cellIndex}>
                            <DocumentDisplay
                                hideProperty
                                property={columnNames[cellIndex]}
                                value={cellItem}
                                kindReferences={kindReferences}
                                kind={kind}
                                datasourceId={datasourceId}
                                datasources={datasources}
                            />
                        </TableCell>
                    ))}
                </TableRow>
            ))}
        </TableBody>
    );
}
