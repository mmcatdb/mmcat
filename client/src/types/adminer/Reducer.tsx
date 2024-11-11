import type { ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';
import type { Datasource } from '@/types/datasource';
import type { View } from '@/types/adminer/View';

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
| DatasourceAction
| KindAction
| ViewAction
| InputAction
| FormAction
| SubmitAction;

type AdminerTypedAction<T extends string, P = undefined> = P extends undefined
  ? { type: T }
  : { type: T } & P;

type InputAction = AdminerTypedAction<'input', {
    field: 'limit';
    value: number;
} | {
    field: 'columnName' | 'columnValue';
    id: number;
    value: string;
} | {
    field: 'operator';
    id: number;
    value: Operator;
}>;

type FormAction = AdminerTypedAction<'form', {
    action: 'add_filter';
} | {
    action: 'delete_filter';
    id: number;
} | {
    action: 'delete_filters';
}>;

type DatasourceAction = AdminerTypedAction<'datasource', { newDatasource: Datasource }>;
type KindAction = AdminerTypedAction<'kind', { newKind: string }>;
type ViewAction = AdminerTypedAction<'view', { newView: View }>;
type SubmitAction = AdminerTypedAction<'submit'>;
