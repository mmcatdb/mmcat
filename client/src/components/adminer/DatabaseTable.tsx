import { useEffect, useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { getTableFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/datasource/Datasource';
import type { TableResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseTableProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseTable({ fetchedData, kindReferences, kind, datasourceId, datasources }: DatabaseTableProps ) {
    const [ tableData, setTableData ] = useState<TableResponse>();

    useEffect(() => {
        setTableData(fetchedData.type === 'graph' ? getTableFromGraphData(fetchedData) : fetchedData);
    }, [ fetchedData ]);

    if (tableData === undefined || tableData.metadata.itemCount === 0)
        return <p>No rows to display.</p>;

    return (
        <>
            {tableData && (
                <Table isStriped isCompact aria-label='Table'>
                    <TableHeader>
                        {tableData.metadata.propertyNames.map((propertyName, index) => (
                            <TableColumn key={index}>{propertyName}</TableColumn>
                        ))}
                    </TableHeader>
                    {TableBodyComponent({ tableBodyData: tableData.data, propertyNames: tableData.metadata.propertyNames, references: kindReferences, kind: kind, datasourceId: datasourceId, datasources: datasources })}
                </Table>
            )
            }
        </>
    );
}

type TableBodyComponentProps = Readonly<{
    tableBodyData: string[][];
    propertyNames: string[];
    references: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

function TableBodyComponent({ tableBodyData, propertyNames, references, kind, datasourceId, datasources }: TableBodyComponentProps ) {
    return (
        <TableBody emptyContent={'No rows to display.'}>
            {tableBodyData.map((row, rowIndex) => (
                <TableRow key={rowIndex}>
                    {row.map((cellItem, cellIndex) => (
                        <TableCell key={cellIndex}>
                            <DocumentComponent valueKey={propertyNames[cellIndex]} value={cellItem} kindReferences={references} kind={kind} datasourceId={datasourceId} datasources={datasources}/>
                        </TableCell>
                    ))}
                </TableRow>
            ))}
        </TableBody>
    );
}
