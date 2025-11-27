import { type AggregatedNumber, type QueryStats } from '@/types/query';
import { type Quantity, type DataSizeUnit, type TimeUnit, dataSizeQuantity, timeQuantity } from '@/types/utils/common';
import { Button, NumberInput } from '@heroui/react';
import { type Dispatch, type SetStateAction, useState } from 'react';
import { UnitSelect } from '../common';
import { type Id } from '@/types/id';
import { api } from '@/api';
import { toast } from 'react-toastify';

type QueryStatsFormProps = {
    queryId: Id;
    stats: QueryStats;
    onSuccess: Dispatch<QueryStats | undefined>;
    onCancel: () => void;
};

/** @deprecated */
export function QueryStatsForm({ queryId, stats, onCancel, onSuccess }: QueryStatsFormProps) {
    const [ state, setState ] = useState(() => dataToForm(stats));

    async function updateStats() {
        const statsEdit = dataFromForm(state, stats);
        const response = await api.queries.updateQuery({ queryId }, { stats: statsEdit });
        if (!response.status) {
            toast.error(`Failed to update query stats: ${response.error}`);
            return;
        }

        toast.success('Query stats updated successfully.');
        onSuccess(response.data.stats ?? undefined);
    }

    // FIXME Number input is such a trash - it don't react to home and end buttons at all.
    // Also, the values are still not correct - e.g., we should disallow negative numbers.

    return (
        <div className='pt-1 flex flex-col gap-3'>
            <div>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    formatOptions={{ maximumFractionDigits: 0 }}
                    className='max-w-50'
                    classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                    labelPlacement='outside'
                    label='Executions'
                    placeholder='No change'
                    value={state.executions}
                    onValueChange={value => setState({ ...state, executions: value })}
                />
            </div>

            {renderAggregatedNumberRow(state, setState, 'dataSize', 'Result size', dataSizeUnits)}
            {renderAggregatedNumberRow(state, setState, 'planningTime', 'Planning time', timeUnits)}
            {renderAggregatedNumberRow(state, setState, 'evaluationTime', 'Evaluation time', timeUnits)}

            {/* <div className='flex items-end gap-2'>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    className='max-w-32'
                    labelPlacement='outside'
                    label='Result size'
                    placeholder='No change'
                    value={state.size}
                    onValueChange={value => setState({ ...state, size: value })}
                />

                <UnitSelect value={state.sizeUnit} units={dataSizeUnits} onChange={unit => setState({ ...state, sizeUnit: unit })} />
            </div> */}

            <div className='mt-2 flex gap-2'>
                <Button onPress={onCancel}>
                    Cancel
                </Button>

                <Button color='success' onPress={updateStats}>
                    Save
                </Button>
            </div>
        </div>
    );
}

function renderAggregatedNumberRow(state: QueryStatsFormState, setState: Dispatch<SetStateAction<QueryStatsFormState>>, numberField: keyof Omit<QueryStatsFormState, 'executions'>, label: string, units: string[]) {
    const number = state[numberField];

    function createFieldSetter<TType>(field: keyof AggregatedNumberFormState<string>): ((value: TType) => void) {
        return value => setState(prev => ({ ...prev, [numberField]: { ...prev[numberField], [field]: value } }));
    }

    return (
        <div>
            <div className='text-sm font-semibold text-foreground-400'>
                {label}
            </div>

            <div className='mt-1 flex flex-wrap gap-y-1'>
                <div className='flex items-end gap-2 w-100'>
                    <NumberInput
                        hideStepper
                        isWheelDisabled
                        className='max-w-24'
                        classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                        labelPlacement='outside'
                        label='Min'
                        placeholder='No change'
                        value={number.min}
                        onValueChange={createFieldSetter('min')}
                    />

                    <NumberInput
                        hideStepper
                        isWheelDisabled
                        className='max-w-24'
                        classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                        labelPlacement='outside'
                        label='Max'
                        placeholder='No change'
                        value={number.max}
                        onValueChange={createFieldSetter('max')}
                    />

                    <UnitSelect value={number.minUnit} units={units} onChange={createFieldSetter('minUnit')} />
                </div>

                <div className='flex items-end gap-2 w-100'>
                    <NumberInput
                        hideStepper
                        isWheelDisabled
                        className='max-w-50'
                        classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                        labelPlacement='outside'
                        label='Sum'
                        placeholder='No change'
                        value={number.sum}
                        onValueChange={createFieldSetter('sum')}
                    />

                    <UnitSelect value={number.sumUnit} units={units} onChange={createFieldSetter('sumUnit')} />
                </div>
            </div>
        </div>
    );
}

const dataSizeUnits = dataSizeQuantity.defineUnits('B', 'TB');
const timeUnits = timeQuantity.defineUnits('ms', 'd');

type QueryStatsFormState = {
    executions: number | undefined;
    dataSize: AggregatedNumberFormState<DataSizeUnit>;
    planningTime: AggregatedNumberFormState<TimeUnit>;
    evaluationTime: AggregatedNumberFormState<TimeUnit>;
};

type AggregatedNumberFormState<TUnit extends string> = {
    min: number | undefined;
    max: number | undefined;
    minUnit: TUnit;
    sum: number | undefined;
    sumUnit: TUnit;
};

function dataToForm(stats: QueryStats): QueryStatsFormState {
    return {
        executions: stats.executionCount,
        dataSize: aggregatedToForm(dataSizeQuantity, stats.resultSizeInBytes),
        planningTime: aggregatedToForm(timeQuantity, stats.planningTimeInMs),
        evaluationTime: aggregatedToForm(timeQuantity, stats.evaluationTimeInMs),
    };
}

function aggregatedToForm<TUnit extends string>(units: Quantity<TUnit>, number: AggregatedNumber): AggregatedNumberFormState<TUnit> {
    const minUnit = units.findUnit(number.min).unit;
    const { value: sum, unit: sumUnit } = units.findUnit(number.sum);

    return {
        min: units.fromBase(number.min, minUnit),
        max: units.fromBase(number.max, minUnit),
        minUnit,
        sum,
        sumUnit,
    };
}

function dataFromForm(form: QueryStatsFormState, prev: QueryStats): QueryStats {
    return {
        executionCount: (form.executions === undefined || isNaN(form.executions)) ? prev.executionCount : form.executions,
        resultSizeInBytes: aggregatedFromForm(dataSizeQuantity, form.dataSize, prev.resultSizeInBytes),
        planningTimeInMs: aggregatedFromForm(timeQuantity, form.planningTime, prev.planningTimeInMs),
        evaluationTimeInMs: aggregatedFromForm(timeQuantity, form.evaluationTime, prev.evaluationTimeInMs),
    };
}

function aggregatedFromForm<TUnit extends string>(quantity: Quantity<TUnit>, form: AggregatedNumberFormState<TUnit>, prev: AggregatedNumber): AggregatedNumber {
    const prevForm = aggregatedToForm(quantity, prev);

    return {
        min: valueFromForm(quantity, form.min, form.minUnit, prevForm.min, prevForm.minUnit, prev.min),
        max: valueFromForm(quantity, form.max, form.minUnit, prevForm.max, prevForm.minUnit, prev.max),
        sum: valueFromForm(quantity, form.sum, form.sumUnit, prevForm.sum, prevForm.sumUnit, prev.sum),
    };
}

function valueFromForm<TUnit extends string>(quantity: Quantity<TUnit>, nextValue: number | undefined, nextUnit: TUnit, prevValue: number | undefined, prevUnit: TUnit, prev: number): number {
    // For some reason, the input returns NaN instead of undefined when cleared.
    if (nextValue === undefined || isNaN(nextValue))
        return prev;
    // If the value didn't change, we want to keep the original value because that might be more precise.
    if (nextValue === prevValue && nextUnit === prevUnit)
        return prev;

    return quantity.toBase(nextValue!, nextUnit);
}
