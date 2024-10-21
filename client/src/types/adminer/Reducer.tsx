import { type ColumnFilter, type Operator } from '@/types/adminer/ColumnFilter';

type KindFilterState = {
    limit: number;
    filters: ColumnFilter[];
}

export type AdminerFilterState = {
    submitted: KindFilterState;
    new: KindFilterState;
}

export type AdminerFilterAction =
| { type: 'submit_state'}
| { type: 'change_limit', newLimit : number }
| { type: 'change_column_name', filterId: number, newName: string }
| { type: 'change_operator', filterId: number, newOperator: Operator }
| { type: 'change_column_value', filterId: number, newValue: string }
| { type: 'add_filter'}
| { type: 'delete_filter', filterID: number}
| { type: 'delete_filters'};
