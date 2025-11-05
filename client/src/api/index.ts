import { actionsApi } from './routes/actions';
import { adminerApi } from './routes/adminer';
import { datasourcesApi } from './routes/datasources';
import { devApi } from './routes/dev';
import { instancesApi } from './routes/instances';
import { jobsApi } from './routes/jobs';
import { mappingsApi } from './routes/mappings';
import { queriesApi } from './routes/queries';
import { schemasApi } from './routes/schemas';

const api = {
    actions: actionsApi,
    adminer: adminerApi,
    datasources: datasourcesApi,
    dev: devApi,
    instances: instancesApi,
    jobs: jobsApi,
    mappings: mappingsApi,
    queries: queriesApi,
    schemas: schemasApi,
};

export { api };
