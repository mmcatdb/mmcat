import { Input, Select, SelectItem, Button } from '@nextui-org/react';
import { IoTrashBin } from 'react-icons/io5';
import type { AdminerStateAction } from '@/types/adminer/Reducer';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

type ColumnFormProps = Readonly<{
    filter: ColumnFilter;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function ColumnForm({ filter, dispatch }: ColumnFormProps) {
    return (
        <div className='mt-0 mr-5 inline-flex gap-1 items-center'>
            <Input
                className='py-0.5 text-sm'
                aria-label='Column name'
                placeholder='Enter column name'
                value={filter.columnName}
                onChange={(e) => dispatch({ type: 'input', field: 'columnName', id: filter.id, value: e.target.value })}
                required
            />

            <Select
                className='py-0.5 text-sm'
                aria-label='Operator'
                placeholder='Select an operator'
                value={filter.operator}
                onChange={(e) => dispatch({ type: 'input', field: 'operator', id: filter.id, value: e.target.value as Operator })}
                required
            >
                {Object.entries(Operator).map(([ key, value ]) => (
                    <SelectItem key={key} value={key}>
                        {value}
                    </SelectItem>
                ))}
            </Select>

            <Input
                className='py-0.5 text-sm'
                aria-label='Column value'
                placeholder='Enter column value'
                value={filter.columnValue}
                onChange={(e) => dispatch({ type: 'input', field: 'columnValue', id: filter.id, value: e.target.value })}
                required
            />

            <Button
                className='py-0.5 text-sm'
                type='submit'
                color='danger'
                variant='ghost'
                onPress={() => {
                    dispatch({ type:'form', action: 'delete_filter', id: filter.id });
                }}
            >
                <IoTrashBin />
            </Button>
        </div>

    );
}
