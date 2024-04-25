import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT, DELETE } from '../routeFunctions';
import type { Datasource, DatasourceInit, DatasourceUpdate, DatasourceInfoFromServer, DatasourceWithConfigurationFromServer } from '@/types/datasource';
import type { Id } from '@/types/id';

const datasources = {
    getAllDatasourceInfos: GET<Empty, DatasourceWithConfigurationFromServer[]>(
        () => `/datasource-infos`,
    ),
    getAllDatasources: GET<Empty, Datasource[], { categoryId: Id }>(
        () => `/datasources`,
    ),
    getDatasource: GET<{ id: StringLike }, Datasource>(
        u => `/datasources/${u.id}`,
    ),
    createDatasource: POST<Empty, DatasourceInfoFromServer, DatasourceInit>(
        () => `/datasources`,
    ),
    updateDatasource: PUT<{ id: StringLike }, DatasourceInfoFromServer, DatasourceUpdate>(
        u => `/datasources/${u.id}`,
    ),
    deleteDatasource: DELETE<{ id: StringLike }, void>(
        u => `/datasources/${u.id}`,
    ),
};

export default datasources;
