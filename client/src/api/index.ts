import actions from './routes/actions';
import adminer from './routes/adminer';
import datasources from './routes/datasources';
import instances from './routes/instances';
import jobs from './routes/jobs';
import mappings from './routes/mappings';
import queries from './routes/queries';
import schemas from './routes/schemas';

const api = {
    actions,
    adminer,
    datasources,
    instances,
    jobs,
    mappings,
    queries,
    schemas,
};

export { api };
