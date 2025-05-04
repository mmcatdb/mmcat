import { Input, Select, SelectItem, Button, Autocomplete, AutocompleteItem } from '@nextui-org/react';
import { IoTrashBin } from 'react-icons/io5';
import { OPERATOR_MAPPING, UNARY_OPERATORS, type Operator } from '@/types/adminer/Operators';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import type { AdminerFilterQueryStateAction } from '@/components/adminer/adminerReducer';
import type { DatasourceType } from '@/types/datasource';

type PropertyFilterProps = Readonly<{
    filter: PropertyFilter;
    datasourceType: DatasourceType;
    propertyNames: string[] | undefined;
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

export function PropertyFilter({ filter, datasourceType, propertyNames, dispatch }: PropertyFilterProps) {
    const operators = typeof OPERATOR_MAPPING[datasourceType] === 'function' && filter.propertyName
        ? OPERATOR_MAPPING[datasourceType]?.(filter.propertyName)
        : OPERATOR_MAPPING[datasourceType];

    return (
        <div className='mt-0 flex flex-wrap gap-1 items-center'>
            <Autocomplete
                className='py-0.5 text-sm w-min min-w-56'
                size='sm'
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
                className='py-0.5 text-sm w-min min-w-36'
                size='sm'
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

            {!UNARY_OPERATORS.includes(filter.operator) && (
                <Input
                    className='py-0.5 text-sm w-min min-w-56'
                    size='sm'
                    aria-label='Column value'
                    placeholder='Enter property value'
                    value={filter.propertyValue}
                    onChange={e => dispatch({ type: 'input', field: 'propertyValue', id: filter.id, value: e.target.value })}
                    required
                />
            )}

            <Button
                className='py-0.5 text-sm min-w-4'
                size='sm'
                aria-label='Delete filter'
                type='submit'
                color='danger'
                variant='bordered'
                onPress={() => {
                    dispatch({ type:'form', action: 'delete_filter', id: filter.id });
                }}
            >
                <IoTrashBin />
            </Button>
        </div>

    );
}
