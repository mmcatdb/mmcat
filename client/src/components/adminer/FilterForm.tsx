import { Button, Input } from '@nextui-org/react';
import { FaSave, FaPlusCircle } from 'react-icons/fa';
import { IoTrashBin } from 'react-icons/io5';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import type { AdminerStateAction, AdminerState } from '@/types/adminer/Reducer';

type FilterFormProps = Readonly<{
    state: AdminerState;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function FilterForm({ state, dispatch }: FilterFormProps) {
    return (
        <div>
            <div>
                {state.form.filters.map((filter) => (
                    <ColumnForm key={filter.id} filter={filter} dispatch={dispatch}/>
                ))}
            </div>

            <div className='mt-5 inline-flex gap-3 items-center'>
                <div className='flex items-center gap-2'>
                    <label htmlFor='limit'>
                        Limit:
                    </label>
                    <Input
                        id='limit'
                        className='text-sm max-h-10'
                        aria-label='Limit'
                        type='number'
                        min='0'
                        placeholder='Enter limit'
                        value={state.form.limit.toString()}
                        onChange={(e) => dispatch({ type: 'change_limit', newLimit: Number(e.target.value) })}
                        required
                    />
                </div>

                <Button
                    className='items-center gap-1 min-w-40'
                    type='submit'
                    color='danger'
                    variant='ghost'
                    onPress={() => {
                        dispatch({ type: 'delete_filters' });
                    }}
                >
                    <IoTrashBin /> Delete filters
                </Button>

                <Button
                    className='items-center gap-1 min-w-40'
                    type='submit'
                    color='success'
                    variant='ghost'
                    onPress={() => dispatch({ type: 'add_filter' })}
                >
                    <FaPlusCircle /> Add filter
                </Button>

                <Button
                    className='items-center gap-1 min-w-40'
                    type='submit'
                    color='primary'
                    onPress={() => dispatch({ type: 'submit_state' })}
                >
                    <FaSave /> SUBMIT
                </Button>
            </div>
        </div>
    );
}
