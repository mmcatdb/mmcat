import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT, DELETE } from '../routeFunctions';
import type { DataSource, DataSourceInit, DataSourceUpdate, DataSourceInfoFromServer, DataSourceWithConfigurationFromServer } from '@/types/dataSource';
import type { Id } from '@/types/id';

const dataSources = {
    getAllDataSourceInfos: GET<Empty, DataSourceWithConfigurationFromServer[]>(
        () => `/data-source-infos`,
    ),
    getAllDataSources: GET<Empty, DataSource[], { categoryId: Id }>(
        () => `/data-sources`,
    ),
    getDataSource: GET<{ id: StringLike }, DataSource>(
        u => `/data-sources/${u.id}`,
    ),
    createDataSource: POST<Empty, DataSourceInfoFromServer, DataSourceInit>(
        () => `/data-sources`,
    ),
    updateDataSource: PUT<{ id: StringLike }, DataSourceInfoFromServer, DataSourceUpdate>(
        u => `/data-sources/${u.id}`,
    ),
    deleteDataSource: DELETE<{ id: StringLike }, void>(
        u => `/data-sources/${u.id}`,
    ),
};

export default dataSources;
