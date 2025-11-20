import { type QueryStats } from '@/types/query';
import { convertDataSize, convertDataSizeToBytes, convertTime, convertTimeToMs, type DataSizeUnit, defineDataSizeUnits, defineTimeUnits, prettyPrintDataSize, prettyPrintTime, type TimeUnit } from '@/types/utils/common';
import { Button, NumberInput } from '@heroui/react';
import { useState } from 'react';
import { UnitSelect } from '../common';
import { type Id } from '@/types/id';
import { api } from '@/api';
import { toast } from 'react-toastify';

type QueryStatsFormProps = {
    queryId: Id;
    stats: QueryStats;
    onSuccess: (stats: QueryStats) => void;
    onCancel: () => void;
};

export function QueryStatsForm({ queryId, stats, onCancel, onSuccess }: QueryStatsFormProps) {
    const [ state, setState ] = useState(() => dataToForm(stats));

    async function updateStats() {
        const edit = dataFromForm(state, stats);
        const response = await api.queries.updateQueryStats({ queryId }, edit);
        if (!response.status) {
            toast.error(`Failed to update query stats: ${response.error}`);
            return;
        }

        toast.success('Query stats updated successfully.');
        onSuccess(response.data.stats);
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
                    className='max-w-32'
                    labelPlacement='outside'
                    label='Executions'
                    placeholder='No change'
                    value={state.executions}
                    onValueChange={value => setState({ ...state, executions: value })}
                />
            </div>

            <div className='flex items-end gap-2'>
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
            </div>

            <div className='flex items-end gap-2'>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    className='max-w-32'
                    labelPlacement='outside'
                    label='Planning time'
                    placeholder='No change'
                    value={state.planning}
                    onValueChange={value => setState({ ...state, planning: value })}
                />

                <UnitSelect value={state.planningUnit} units={timeUnits} onChange={unit => setState({ ...state, planningUnit: unit })} />
            </div>

            <div className='flex items-end gap-2'>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    className='max-w-32'
                    labelPlacement='outside'
                    label='Evaluation time'
                    placeholder='No change'
                    value={state.evaluation}
                    onValueChange={value => setState({ ...state, evaluation: value })}
                />

                <UnitSelect value={state.evaluationUnit} units={timeUnits} onChange={unit => setState({ ...state, evaluationUnit: unit })} />
            </div>

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

const dataSizeUnits = defineDataSizeUnits('B', 'GB');
const timeUnits = defineTimeUnits('ms', 'h');

type QueryStatsFormState = {
    executions: number | undefined;
    size: number | undefined;
    sizeUnit: DataSizeUnit;
    planning: number | undefined;
    planningUnit: TimeUnit;
    evaluation: number | undefined;
    evaluationUnit: TimeUnit;
};

function dataToForm(stats: QueryStats): QueryStatsFormState {
    const resultSizeUnit = prettyPrintDataSize(stats.resultSizeSumInBytes).split(' ')[1] as DataSizeUnit;
    const planningTimeUnit = prettyPrintTime(stats.planningTimeSumInMs).split(' ')[1] as TimeUnit;
    const evaluationTimeUnit = prettyPrintTime(stats.evaluationTimeSumInMs).split(' ')[1] as TimeUnit;

    return {
        executions: stats.executionCount,
        size: convertDataSize(stats.resultSizeSumInBytes, resultSizeUnit),
        sizeUnit: resultSizeUnit,
        planning: convertTime(stats.planningTimeSumInMs, planningTimeUnit),
        planningUnit: planningTimeUnit,
        evaluation: convertTime(stats.evaluationTimeSumInMs, evaluationTimeUnit),
        evaluationUnit: evaluationTimeUnit,
    };
}

function dataFromForm(state: QueryStatsFormState, prevStats: QueryStats): QueryStats {
    const prev = dataToForm(prevStats);

    return {
        executionCount: (state.executions === undefined || Number.isNaN(state.executions)) ? prevStats.executionCount : state.executions,
        resultSizeSumInBytes: isChanged(state.size, state.sizeUnit, prev.size, prev.sizeUnit)
            ? convertDataSizeToBytes(state.size!, state.sizeUnit)
            : prevStats.resultSizeSumInBytes,
        planningTimeSumInMs: isChanged(state.planning, state.planningUnit, prev.planning, prev.planningUnit)
            ? convertTimeToMs(state.planning!, state.planningUnit)
            : prevStats.planningTimeSumInMs,
        evaluationTimeSumInMs: isChanged(state.evaluation, state.evaluationUnit, prev.evaluation, prev.evaluationUnit)
            ? convertTimeToMs(state.evaluation!, state.evaluationUnit)
            : prevStats.evaluationTimeSumInMs,
    };
}

function isChanged(nextValue: number | undefined, nextUnit: string, prevValue: number | undefined, prevUnit: string): boolean {
    // For some reason, the input returns NaN instead of undefined when cleared.
    if (nextValue === undefined || Number.isNaN(nextValue))
        return false;

    // If the value didn't change, we want to keep the original value because that might be more precise.
    return nextValue !== prevValue || nextUnit !== prevUnit;
}
