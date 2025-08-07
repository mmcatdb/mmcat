import { Input, Select, SelectItem, Button, Autocomplete, AutocompleteItem } from '@heroui/react';
import { IoTrashBin } from 'react-icons/io5';
import { OPERATOR_MAPPING, UNARY_OPERATORS, type Operator } from '@/types/adminer/Operators';
import { type PropertyFilter } from './URLParamsState';
import type { AdminerFilterQueryStateAction } from '@/components/adminer/adminerReducer';
import type { DatasourceType } from '@/types/Datasource';
import { useMemo, type Dispatch } from 'react';

type PropertyFilterFormProps = {
    /** The filter to be updated. */
    filter: PropertyFilter;
    /** The type of selected datasource. */
    datasourceType: DatasourceType;
    /** Names of properties. */
    propertyNames: string[] | undefined;
    /** A function for state updating. */
    dispatch: Dispatch<AdminerFilterQueryStateAction>;
};

/**
 * Component for updating a property filter
 */
export function PropertyFilterForm({ filter, datasourceType, propertyNames, dispatch }: PropertyFilterFormProps) {
    const propertyOptions = useMemo(() => propertyNames?.map(propertyName => ({ key: propertyName, label: propertyName })) ?? [], [ propertyNames ]);

    const operatorOptions = useMemo(() => {
        const operators = OPERATOR_MAPPING[datasourceType]?.(filter.propertyName);
        if (!operators)
            return [];

        return Object.entries(operators).map(([ key, label ]) => ({ key, label }));
    }, [ datasourceType, filter.propertyName ]);

    return (
        <div className='flex flex-wrap gap-x-2 gap-y-1 items-center'>
            <Autocomplete
                items={propertyOptions}
                className='text-sm w-min min-w-56'
                size='sm'
                aria-label='Column name'
                placeholder='Enter property name'
                selectedKey={filter.propertyName || null}
                onSelectionChange={key => dispatch({ type: 'input', field: 'propertyName', id: filter.id, value: key ? String(key) : '' })}
                required
            >
                {(propertyNames ?? []).map(propertyName => (
                    <AutocompleteItem key={propertyName}>{propertyName}</AutocompleteItem>
                ))}
            </Autocomplete>

            <Select
                items={operatorOptions}
                className='text-sm w-min min-w-36'
                size='sm'
                aria-label='Operator'
                placeholder='Select an operator'
                selectedKeys={filter.operator ? [ filter.operator ] : []}
                onChange={e => dispatch({ type: 'input', field: 'operator', id: filter.id, value: e.target.value as Operator })}
                required
            >
                {item => (
                    <SelectItem key={item.key}>
                        {item.label}
                    </SelectItem>
                )}
            </Select>

            {filter.operator && !UNARY_OPERATORS.includes(filter.operator) && (
                <Input
                    className='text-sm w-min min-w-56'
                    size='sm'
                    aria-label='Column value'
                    placeholder='Enter property value'
                    value={filter.propertyValue}
                    onChange={e => dispatch({ type: 'input', field: 'propertyValue', id: filter.id, value: e.target.value })}
                    required
                />
            )}

            <Button
                className='text-sm min-w-4'
                size='sm'
                aria-label='Delete filter'
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
