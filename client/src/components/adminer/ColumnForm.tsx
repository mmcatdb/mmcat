import { Input, Select, SelectItem } from '@nextui-org/react';
import type { Action } from '@/types/adminer/Reducer';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

type ColumnFormProps = Readonly<{
    filter: ColumnFilter;
    dispatch: React.Dispatch<Action>;
}>;

export function ColumnForm({ filter, dispatch }: ColumnFormProps) {
    return (
        <div className='mt-5 flex gap-3 items-center'>
            <Input
                label='Column Name'
                placeholder='Enter column name'
                value={filter.columnName}
                onChange={(e) => dispatch({ type: 'change_column_name', filterId: filter.id, newName: e.target.value })}
                required
            />

            <Select
                label='Operator'
                placeholder='Select an operator'
                value={filter.operator}
                onChange={(e) => dispatch({ type: 'change_operator', filterId: filter.id, newOperator: e.target.value as Operator })}
                required
            >
                {Object.entries(Operator).map(([ key, value ]) => (
                    <SelectItem key={key} value={key}>
                        {value}
                    </SelectItem>
                ))}
            </Select>

            <Input
                label='Column Value'
                placeholder='Enter column value'
                value={filter.columnValue}
                onChange={(e) => dispatch({ type: 'change_column_value', filterId: filter.id, newValue: e.target.value })}
                required
            />
        </div>
    );
}
