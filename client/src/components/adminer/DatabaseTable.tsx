import { useMemo, useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { getTableFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/datasource/Datasource';
import type { TableResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseTableProps = Readonly<{
    data: TableResponse | GraphResponse;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseTable({ data, kindReferences, kind, datasourceId, datasources }: DatabaseTableProps ) {
    const [ tableData, setTableData ] = useState<TableResponse>();
    const [ tableColumnNames, setTableColumnNames ] = useState<string[]>();

    useMemo(() => {
        const { reshapedData, columnNames } = data.type === 'graph'
            ? getTableFromGraphData(data)
            : { reshapedData: data, columnNames: data.metadata.propertyNames };

        setTableData(reshapedData);
        setTableColumnNames(columnNames);
    }, [ data ]);

    if (tableData === undefined || tableData.data.length === 0 || tableData.metadata.itemCount === 0)
        return <p>No rows to display.</p>;

    return (
        <>
            {tableData && tableColumnNames && (
                <Table isStriped isCompact aria-label='Table'>
                    <TableHeader>
                        {tableColumnNames.map((columnName, index) => (
                            <TableColumn key={index}>{columnName}</TableColumn>
                        ))}
                    </TableHeader>
                    {TableBodyComponent({ tableBodyData: tableData.data, columnNames: tableColumnNames, references: kindReferences, kind: kind, datasourceId: datasourceId, datasources: datasources })}
                </Table>
            )
            }
        </>
    );
}

type TableBodyComponentProps = Readonly<{
    tableBodyData: string[][];
    columnNames: string[];
    references: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

function TableBodyComponent({ tableBodyData, columnNames, references, kind, datasourceId, datasources }: TableBodyComponentProps ) {
    return (
        <TableBody emptyContent={'No rows to display.'}>
            {tableBodyData.map((row, rowIndex) => (
                <TableRow key={rowIndex}>
                    {row.map((cellItem, cellIndex) => (
                        <TableCell key={cellIndex}>
                            <DocumentComponent valueKey={columnNames[cellIndex]} value={cellItem} kindReferences={references} kind={kind} datasourceId={datasourceId} datasources={datasources}/>
                        </TableCell>
                    ))}
                </TableRow>
            ))}
        </TableBody>
    );
}
