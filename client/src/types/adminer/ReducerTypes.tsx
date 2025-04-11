import type { Operator } from '@/types/adminer/Operators';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import type { Datasource } from '@/types/datasource';
import type { View } from '@/types/adminer/View';
import type { Id } from '@/types/id';

export type AdminerCustomQueryState = AdminerStateBase & {
    query: string;
}

export type AdminerFilterQueryState = ActiveAdminerState & {
    form: KindFilterState;
};

export type ActiveAdminerState = {
    active: KindFilterState;
    kindName?: string;
    view: View;
} & AdminerStateBase

export type KindFilterState = {
    limit: number;
    offset: number;
    filters: PropertyFilter[];
}

type AdminerStateBase = {
    datasourceId?: Id;
}

export type AdminerCustomQueryStateAction =
| DatasourceAction
| QueryAction
| InitializeAction
| CustomQueryUpdateAction;

export type AdminerFilterQueryStateAction =
| DatasourceAction
| KindAction
| ViewAction
| InputAction
| FormAction
| SubmitAction
| InitializeAction
| FilterQueryUpdateAction;

type AdminerTypedAction<T extends string, P = undefined> = P extends undefined
  ? { type: T }
  : { type: T } & P;

export type InputAction = AdminerTypedAction<'input', {
    field: 'limit' | 'offset';
    value: number;
} | {
    field: 'propertyName' | 'propertyValue';
    id: number;
    value: string;
} | {
    field: 'operator';
    id: number;
    value: Operator;
}>;

export type FormAction = AdminerTypedAction<'form', {
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
type InitializeAction = AdminerTypedAction<'initialize'>;
type FilterQueryUpdateAction = AdminerTypedAction<'update', {newState: AdminerFilterQueryState }>;
type QueryAction = AdminerTypedAction<'query', { newQuery: string }>;
type CustomQueryUpdateAction = AdminerTypedAction<'update', {newState: AdminerCustomQueryState }>;
