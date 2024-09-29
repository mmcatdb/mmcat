import { type ColumnFilter, type Operator } from '@/types/adminer/ColumnFilter';

export type State = {
    limit: number;
    filters: ColumnFilter[];
}

export type Action =
| { type: 'change_limit', newLimit : number }
| { type: 'change_column_name', filterId: number, newName: string }
| { type: 'change_operator', filterId: number, newOperator: Operator }
| { type: 'change_column_value', filterId: number, newValue: string }
| { type: 'add_filter'}
| { type: 'delete_filters'};
