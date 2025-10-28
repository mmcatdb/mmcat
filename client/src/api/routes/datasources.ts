import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT, DELETE } from '../routeFunctions';
import type { DatasourceInit, DatasourceUpdate, DatasourceResponse } from '@/types/Datasource';
import type { Id } from '@/types/id';

export const datasourcesApi = {
    getAllDatasources: GET<Empty, DatasourceResponse[], { categoryId: Id }>(
        () => `/datasources`,
    ),
    getDatasource: GET<{ id: StringLike }, DatasourceResponse>(
        u => `/datasources/${u.id}`,
    ),
    getDatasourceForMapping: GET<Empty, DatasourceResponse, { mappingId: Id }>(
        () => `/datasources/for-mapping`,
    ),
    createDatasource: POST<Empty, DatasourceResponse, DatasourceInit>(
        () => `/datasources`,
    ),
    updateDatasource: PUT<{ id: StringLike }, DatasourceResponse, DatasourceUpdate>(
        u => `/datasources/${u.id}`,
    ),
    deleteDatasource: DELETE<{ id: StringLike }, void>(
        u => `/datasources/${u.id}`,
    ),
};
