import type { Entity, Id, VersionId } from './id';

export type QueryFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
};

export class Query implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly label: string,
    ) {}

    static fromServer(input: QueryFromServer): Query {
        return new Query(
            input.id,
            input.categoryId,
            input.label,
        );
    }
}

export type QueryVersionFromServer = {
    id: Id;
    queryId: Id;
    version: VersionId;
    content: string;
};

export class QueryVersion implements Entity {
    private constructor(
        readonly id: Id,
        readonly query: Query,
        readonly version: VersionId,
        readonly content: string,
    ) {}

    static fromServer(input: QueryVersionFromServer, query: Query): QueryVersion {
        return new QueryVersion(
            input.id,
            query,
            input.version,
            input.content,
        );
    }
}

export type QueryVersionInit = {
    version: VersionId;
    content: string;
};

export type QueryInit = QueryVersionInit & {
    categoryId: Id;
    label: string;
};

export type QueryWithVersionFromServer = {
    query: QueryFromServer;
    version: QueryVersionFromServer;
};

export type QueryWithVersion = {
    query: Query;
    version: QueryVersion;
};

export function queryWithVersionFromServer(qv: QueryWithVersionFromServer): QueryWithVersion {
    const query = Query.fromServer(qv.query);
    const version = QueryVersion.fromServer(qv.version, query);

    return { query, version };
}

export type QueryWithVersionsFromServer = {
    query: QueryFromServer;
    versions: QueryVersionFromServer[];
};

