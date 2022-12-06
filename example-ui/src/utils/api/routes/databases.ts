import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST, PUT, DELETE } from "../routeFunctions";
import type { Database, DatabaseInit, DatabaseUpdate, DatabaseViewFromServer, DatabaseWithConfigurationFromServer } from "@/types/database";

const databases = {
    getAllDatabaseViews: GET<Empty, DatabaseWithConfigurationFromServer[]>(
        () => `/database-views`
    ),
    getAllDatabases: GET<Empty, Database[]>(
        () => `/databases`
    ),
    getDatabase: GET<{ id: StringLike }, Database>(
        u => `/databases/${u.id}`
    ),
    createDatabase: POST<Empty, DatabaseViewFromServer, DatabaseInit>(
        () => `/databases`
    ),
    updateDatabase: PUT<{ id: StringLike }, DatabaseViewFromServer, DatabaseUpdate>(
        u => `/databases/${u.id}`
    ),
    deleteDatabase: DELETE<{ id: StringLike }, void>(
        u => `/databases/${u.id}`
    )
};

export default databases;
