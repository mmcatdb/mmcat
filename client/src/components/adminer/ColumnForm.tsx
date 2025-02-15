import { Input, Select, SelectItem, Button, Autocomplete, AutocompleteItem } from '@nextui-org/react';
import { IoTrashBin } from 'react-icons/io5';
import { OPERATOR_MAPPING, type Operator } from '@/types/adminer/Operators';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import type { AdminerStateAction } from '@/types/adminer/Reducer';
import { type DatasourceType } from '@/types/datasource';

type ColumnFormProps = Readonly<{
    filter: PropertyFilter;
    datasourceType: DatasourceType;
    propertyNames: string[] | undefined;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function ColumnForm({ filter, datasourceType, propertyNames, dispatch }: ColumnFormProps) {
    const operators = OPERATOR_MAPPING[datasourceType];
    return (
        <div className='mt-0 mr-5 inline-flex gap-1 items-center'>
            <Autocomplete
                className='py-0.5 text-sm'
                aria-label='Column name'
                placeholder='Enter property name'
                defaultSelectedKey={ filter.propertyName ?? undefined }
                onSelectionChange={key => dispatch({ type: 'input', field: 'propertyName', id: filter.id, value: key ? String(key) : '' })}
                required
            >
                {(propertyNames ?? []).map(propertyName => (
                    <AutocompleteItem key={propertyName}>{propertyName}</AutocompleteItem>
                ))}
            </Autocomplete>

            <Select
                className='py-0.5 text-sm'
                aria-label='Operator'
                placeholder='Select an operator'
                defaultSelectedKeys={ [ filter.operator ] }
                onChange={e => dispatch({ type: 'input', field: 'operator', id: filter.id, value: e.target.value as Operator })}
                required
            >
                {operators ? Object.entries(operators).map(([ key, label ]) => (
                    <SelectItem key={key} value={key}>
                        {label}
                    </SelectItem>
                )) : []}
            </Select>

            <Input
                className='py-0.5 text-sm'
                aria-label='Column value'
                placeholder='Enter property value'
                value={filter.propertyValue}
                onChange={e => dispatch({ type: 'input', field: 'propertyValue', id: filter.id, value: e.target.value })}
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
