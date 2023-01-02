import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST, PUT, DELETE } from "../routeFunctions";
import type { Id } from "@/types/id";
import type { DataSource, DataSourceFromServer, DataSourceInit, DataSourceUpdate } from "@/types/dataSource";

const dataSources = {
    getAllDataSources: GET<Empty, DataSource[], { categoryId: Id }>(
        () => `/data-sources`
    ),
    getDataSource: GET<{ id: StringLike }, DataSource>(
        u => `/data-sources/${u.id}`
    ),
    createDataSource: POST<Empty, DataSourceFromServer, DataSourceInit>(
        () => `/data-sources`
    ),
    updateDataSource: PUT<{ id: StringLike }, DataSourceFromServer, DataSourceUpdate>(
        u => `/data-sources/${u.id}`
    ),
    deleteDataSource: DELETE<{ id: StringLike }, void>(
        u => `/data-sources/${u.id}`
    )
};

export default dataSources;
