import { actionsApi } from './routes/actions';
import { adaptationsApi } from './routes/adaptations';
import { adminerApi } from './routes/adminer';
import { datasourcesApi } from './routes/datasources';
import { devApi } from './routes/dev';
import { filesApi } from './routes/files';
import { instancesApi } from './routes/instances';
import { jobsApi } from './routes/jobs';
import { mappingsApi } from './routes/mappings';
import { queriesApi } from './routes/queries';
import { schemasApi } from './routes/schemas';

const api = {
    actions: actionsApi,
    adaptations: adaptationsApi,
    adminer: adminerApi,
    datasources: datasourcesApi,
    dev: devApi,
    files: filesApi,
    instances: instancesApi,
    jobs: jobsApi,
    mappings: mappingsApi,
    queries: queriesApi,
    schemas: schemasApi,
};

export { api };
