import databases from './routes/databases';
import dataSources from './routes/dataSource';
import instances from './routes/instances';
import jobs from './routes/jobs';
import logicalModels from './routes/logicalModels';
import mappings from './routes/mappings';
import models from './routes/models';
import queries from './routes/queries';
import schemas from './routes/schemas';

const API = {
    databases,
    dataSources,
    instances,
    jobs,
    logicalModels,
    mappings,
    models,
    queries,
    schemas,
};

export default API;