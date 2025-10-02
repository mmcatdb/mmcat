import { actionsApi } from './routes/actions';
import { adminerApi } from './routes/adminer';
import { datasourcesApi } from './routes/datasources';
import { instancesApi } from './routes/instances';
import { jobsApi } from './routes/jobs';
import { mappingsApi } from './routes/mappings';
import { queriesApi } from './routes/queries';
import { schemasApi } from './routes/schemas';

const api = {
    actions: actionsApi,
    adminer: adminerApi,
    datasources: datasourcesApi,
    instances: instancesApi,
    jobs: jobsApi,
    mappings: mappingsApi,
    queries: queriesApi,
    schemas: schemasApi,
};

export { api };
