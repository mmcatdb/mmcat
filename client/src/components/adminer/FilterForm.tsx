import { useEffect } from 'react';
import { Button, Input } from '@nextui-org/react';
import { FaSave, FaPlusCircle } from 'react-icons/fa';
import { IoTrashBin } from 'react-icons/io5';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import type { AdminerFilterQueryStateAction, AdminerFilterQueryState } from '@/components/adminer/filterQueryReducer';
import type { DatasourceType } from '@/types/datasource/Datasource';

type FilterFormProps = Readonly<{
    state: AdminerFilterQueryState;
    datasourceType: DatasourceType;
    propertyNames: string[] | undefined;
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

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

    return (
        <>
            <>
                {state.form.filters.map(filter => (
                    <ColumnForm key={filter.id} filter={filter} datasourceType={datasourceType} propertyNames={propertyNames} dispatch={dispatch}/>
                ))}
            </>

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
                    aria-label='Delete filters'
                    type='submit'
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
                    aria-label='Add filter'
                    type='submit'
                    color='success'
                    variant='bordered'
                    onPress={() => dispatch({ type: 'form', action: 'add_filter' })}
                >
                    <FaPlusCircle /> Add filter
                </Button>

                <Button
                    className='items-center gap-1 min-w-40'
                    size='sm'
                    aria-label='Submit filters'
                    type='submit'
                    color='primary'
                    onPress={() => dispatch({ type: 'submit' })}
                >
                    <FaSave /> SUBMIT
                </Button>
            </div>
        </>
    );
}
