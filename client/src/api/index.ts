import actions from './routes/actions';
import datasources from './routes/datasources';
import instances from './routes/instances';
import jobs from './routes/jobs';
import mappings from './routes/mappings';
import queries from './routes/queries';
import schemas from './routes/schemas';

const api = {
    actions,
    datasources,
    instances,
    jobs,
    mappings,
    queries,
    schemas,
};

export { api };

export type Resolved<TLoaderData extends Record<string, unknown>, TKey extends keyof TLoaderData> = TLoaderData[TKey] extends Promise<infer TResolved> ? TResolved : never;
