import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST, PUT, DELETE } from "../routeFunctions";
import type { Database, DatabaseInit, DatabaseUpdate, DatabaseInfoFromServer, DatabaseWithConfigurationFromServer } from "@/types/database";

const databases = {
    getAllDatabaseInfos: GET<Empty, DatabaseWithConfigurationFromServer[]>(
        () => `/database-infos`
    ),
    getAllDatabases: GET<Empty, Database[], { categoryId: number }>(
        () => `/databases`
    ),
    getDatabase: GET<{ id: StringLike }, Database>(
        u => `/databases/${u.id}`
    ),
    createDatabase: POST<Empty, DatabaseInfoFromServer, DatabaseInit>(
        () => `/databases`
    ),
    updateDatabase: PUT<{ id: StringLike }, DatabaseInfoFromServer, DatabaseUpdate>(
        u => `/databases/${u.id}`
    ),
    deleteDatabase: DELETE<{ id: StringLike }, void>(
        u => `/databases/${u.id}`
    )
};

export default databases;
