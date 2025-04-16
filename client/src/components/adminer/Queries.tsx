import { DatasourceType } from '@/types/datasource';

export const EXAMPLE_QUERY: Record<DatasourceType, string> = {
    [DatasourceType.neo4j]: 'MATCH (n) RETURN n;',
    [DatasourceType.mongodb]: '{ find: \'address\' }',
    [DatasourceType.postgresql]: 'SELECT * FROM "order";',
    [DatasourceType.csv]: '',
    [DatasourceType.json]: '',
    [DatasourceType.jsonld]: '',
};
