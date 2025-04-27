import { DatasourceType } from '@/types/datasource';

export const EXAMPLE_QUERY: Record<DatasourceType, string> = {
    [DatasourceType.neo4j]: 'MATCH (u)-[:FRIEND]->(f)-[:FRIEND]->(fof) WHERE f.id = \'user_005\' RETURN u, f, fof;',
    [DatasourceType.mongodb]: '{"find": "business", "filter": {"attributes.wifi": "free"}}',
    [DatasourceType.postgresql]: 'SELECT u.name, u.fans, c.business_id, c.date, c.text, c.stars FROM "user" u JOIN "comment" c ON (u.user_id = c.user_id) WHERE c.stars >= 2;',
    [DatasourceType.csv]: '',
    [DatasourceType.json]: '',
    [DatasourceType.jsonld]: '',
};
