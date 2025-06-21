import { useEffect } from 'react';
import { Button, Input } from '@nextui-org/react';
import { FaSave, FaPlusCircle } from 'react-icons/fa';
import { IoTrashBin } from 'react-icons/io5';
import { PropertyFilterForm } from '@/components/adminer/PropertyFilterForm';
import type { AdminerFilterQueryStateAction, AdminerFilterQueryState } from '@/components/adminer/adminerReducer';
import type { DatasourceType } from '@/types/datasource/Datasource';

type FilterFormProps = Readonly<{
    /** A state that stores data selected by user. */
    state: AdminerFilterQueryState;
    /** The type of selected datasource. */
    datasourceType: DatasourceType;
    /** Names of properties. */
    propertyNames: string[] | undefined;
    /** A function for state updating. */
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

/**
 * Component for getting filters from user
 */
export function FilterForm({ state, datasourceType, propertyNames, dispatch }: FilterFormProps) {
    useEffect(() => {
        const handleKeyPress = (e: KeyboardEvent) => {
            if (e.key === 'Enter')
                dispatch({ type: 'submit' });

        };

        document.addEventListener('keydown', handleKeyPress);
        return () => {
            document.removeEventListener('keydown', handleKeyPress);
        };
    });

    return (<>
        {state.form.propertyFilters.map(filter => (
            <PropertyFilterForm key={filter.id} filter={filter} datasourceType={datasourceType} propertyNames={propertyNames} dispatch={dispatch}/>
        ))}

        <div className='mt-1 flex flex-wrap gap-2 items-center'>
            <label htmlFor='limit'>
                    Limit:
            </label>
            <Input
                id='limit'
                className='text-sm max-h-10 w-min min-w-24'
                type='number'
                min='0'
                label='Limit'
                labelPlacement='outside-left'
                classNames={
                    { label:'sr-only' }
                }
                size='sm'
                placeholder='Enter limit'
                value={state.form.limit.toString()}
                onChange={e => dispatch({ type: 'input', field: 'limit', value: Number(e.target.value) })}
                required
            />

            <Button
                className='items-center gap-1 min-w-40'
                size='sm'
                color='danger'
                variant='bordered'
                onPress={() => {
                    dispatch({ type: 'form', action: 'delete_filters' });
                }}
            >
                <IoTrashBin /> Delete filters
            </Button>

            <Button
                className='items-center gap-1 min-w-40'
                size='sm'
                color='success'
                variant='bordered'
                onPress={() => dispatch({ type: 'form', action: 'add_filter' })}
            >
                <FaPlusCircle /> Add filter
            </Button>

            <Button
                className='items-center gap-1 min-w-40'
                size='sm'
                color='primary'
                onPress={() => dispatch({ type: 'submit' })}
            >
                <FaSave /> SUBMIT
            </Button>
        </div>
    </>);
}
