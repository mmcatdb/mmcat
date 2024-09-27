import { Input, Select, SelectItem, Button, Spacer } from '@nextui-org/react';
import { useState } from 'react';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

type ColumnFormProps = Readonly<{
    actualFilter: ColumnFilter;
    filters: ColumnFilter[] | undefined;
    setFilters: (newFilters: ColumnFilter[]) => void;
}>;

export function ColumnForm({ actualFilter, filters, setFilters }: ColumnFormProps) {
    const [ columnName, setColumnName ] = useState<string>(actualFilter.columnName);
    const [ columnValue, setColumnValue ] = useState<string>(actualFilter.columnValue);
    const [ operator, setOperator ] = useState<Operator>(actualFilter.operator);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const filter: ColumnFilter = {
            columnName,
            columnValue,
            operator,
        };

        setFilters(filters ? [ ...filters, filter ] : [ filter ]);

        setColumnName(actualFilter.columnName);
        setColumnValue(actualFilter.columnValue);
        setOperator(actualFilter.operator);
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <Input
                label='Column Name'
                placeholder='Enter column name'
                value={columnName}
                onChange={(e) => setColumnName(e.target.value)}
                required
            />

            <Spacer y={1} />

            <Select
                label='Operator'
                placeholder='Select an operator'
                value={operator}
                onChange={(e) => setOperator(e.target.value as Operator)}
                required
            >
                {Object.entries(Operator).map(([ key, value ]) => (
                    <SelectItem key={key} value={key}>
                        {value}
                    </SelectItem>
                ))}
            </Select>

            <Spacer y={1} />

            <Input
                label='Column Value'
                placeholder='Enter column value'
                value={columnValue}
                onChange={(e) => setColumnValue(e.target.value)}
                required
            />

            <Spacer y={1.5} />

            <Button type='submit' color='primary'>
                Submit
            </Button>
        </form>
    );
}
