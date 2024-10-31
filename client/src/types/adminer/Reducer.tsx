import { type ColumnFilter, type Operator } from '@/types/adminer/ColumnFilter';
import { type Datasource } from '@/types/datasource';
import { type View } from '@/types/adminer/View';

type KindFilterState = {
    limit: number;
    filters: ColumnFilter[];
}

export type AdminerState = {
    form: KindFilterState;
    active: KindFilterState;
    datasource?: Datasource;
    kind?: string;
    view: View;
}

export type AdminerStateAction =
| { type: 'datasource', newDatasource : Datasource}
| { type: 'kind', newKind : string}
| { type: 'view', newView : View}
| { type: 'submit_state'}
| { type: 'change_limit', newLimit : number }
| { type: 'change_column_name', filterId: number, newName: string }
| { type: 'change_operator', filterId: number, newOperator: Operator }
| { type: 'change_column_value', filterId: number, newValue: string }
| { type: 'add_filter'}
| { type: 'delete_filter', filterID: number}
| { type: 'delete_filters'};
