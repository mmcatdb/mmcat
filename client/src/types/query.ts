import { DatasourceInfo, type DatasourceInfoFromServer } from './datasource';
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
    errors: QueryUpdateError[];
};

export class QueryVersion implements Entity {
    private constructor(
        readonly id: Id,
        readonly query: Query,
        readonly version: VersionId,
        readonly content: string,
        readonly errors: QueryUpdateError[],
    ) {}

    static fromServer(input: QueryVersionFromServer, query: Query): QueryVersion {
        return new QueryVersion(
            input.id,
            query,
            input.version,
            input.content,
            input.errors,
        );
    }
}

export type QueryVersionUpdate = {
    version: VersionId;
    content: string;
    errors: QueryUpdateError[];
};

export type QueryInit = Omit<QueryVersionUpdate, 'errors'> & {
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

// Evolution

export enum ErrorType {
    ParseError = 'ParseError',
    UpdateWarning = 'UpdateWarning',
    UpdateError = 'UpdateError',
}

export type QueryUpdateError = {
    type: ErrorType;
    message: string;
    data: unknown;
};

export type QueryDescriptionFromServer = {
    parts: QueryPartDescriptionFromServer[];
};

type QueryPartDescriptionFromServer = {
    datasource: DatasourceInfoFromServer;
    content: string;
    structure: string;
};

export class QueryDescription {
    private constructor(
        readonly parts: QueryPartDescription[],
    ) {}

    static fromServer(input: QueryDescriptionFromServer): QueryDescription {
        return new QueryDescription(
            input.parts.map(QueryPartDescription.fromServer),
        );
    }
}

export class QueryPartDescription {
    private constructor(
        readonly datasource: DatasourceInfo,
        readonly content: string,
        readonly structure: string,
    ) {}

    static fromServer(input: QueryPartDescriptionFromServer): QueryPartDescription {
        return new QueryPartDescription(
            DatasourceInfo.fromServer(input.datasource),
            input.content,
            input.structure,
        );
    }
}

type QueryStructure = {
    name: string;
    isArray: boolean;
    children: Map<string, QueryStructure>;
    /** If null, this is the root of the tree. */
    parent?: QueryStructure;
};
