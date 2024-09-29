import { Button, Input } from '@nextui-org/react';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import type { Action, State } from '@/types/adminer/Reducer';

type FilterFormProps = Readonly<{
    state: State;
    dispatch: React.Dispatch<Action>;
    setNewState: (state: State) => void;
}>;

export function FilterForm({ state, dispatch, setNewState }: FilterFormProps) {
    return (
        <div>
            <div className='mt-5 flex flex-wrap gap-3 items-center'>
                <Input
                    type='number'
                    min='0'
                    label='Limit'
                    placeholder='Enter limit'
                    value={state.limit.toString()}
                    onChange={(e) => dispatch({ type:'change_limit', newLimit: Number(e.target.value) })}
                    required
                />
            </div>

            {state.filters.map((filter) => (
                <ColumnForm key={filter.id} filter={filter} dispatch={dispatch}/>
            ))}

            <div className='mt-5 flex flex-wrap gap-3 items-center'>
                <Button
                    type='submit'
                    color='primary'
                    onPress={() => setNewState(state)}
                >
                Submit
                </Button>

                <Button
                    type='submit'
                    color='primary'
                    onPress={() => {
                        dispatch({ type:'delete_filters' });
                        setNewState({ limit: state.limit, filters: [] });
                    }}
                >
                Delete filters
                </Button>

                <Button
                    type='submit'
                    color='primary'
                    onPress={() => dispatch({ type:'add_filter' })}
                >
                Add filter
                </Button>
            </div>
        </div>
    );
}
