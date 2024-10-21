import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT, DELETE } from '../routeFunctions';
import type { DatasourceInit, DatasourceUpdate, DatasourceFromServer } from '@/types/datasource';
import type { Id } from '@/types/id';

const datasources = {
    getAllDatasources: GET<Empty, DatasourceFromServer[], GetDatasourcesQuery>(
        () => `/datasources`,
    ),
    getDatasource: GET<{ id: StringLike }, DatasourceFromServer>(
        u => `/datasources/${u.id}`,
    ),
    createDatasource: POST<Empty, DatasourceFromServer, DatasourceInit>(
        () => `/datasources`,
    ),
    updateDatasource: PUT<{ id: StringLike }, DatasourceFromServer, DatasourceUpdate>(
        u => `/datasources/${u.id}`,
    ),
    deleteDatasource: DELETE<{ id: StringLike }, void>(
        u => `/datasources/${u.id}`,
    ),
};

export default datasources;

type GetDatasourcesQuery = {
    categoryId?: Id;
};
