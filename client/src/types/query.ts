import { Datasource, type DatasourceFromServer } from './datasource';
import type { Entity, Id, VersionId } from './id';

export type QueryFromServer = {
    id: Id;
    version: VersionId;
    lastValid: VersionId;
    categoryId: Id;
    label: string;
    content: string;
    errors: QueryEvolutionError[];
};

export class Query implements Entity {
    private constructor(
        readonly id: Id,
        readonly version: VersionId,
        readonly lastValid: VersionId,
        readonly categoryId: Id,
        readonly label: string,
        readonly content: string,
        readonly errors: QueryEvolutionError[],
    ) {}

    static fromServer(input: QueryFromServer): Query {
        return new Query(
            input.id,
            input.version,
            input.lastValid,
            input.categoryId,
            input.label,
            input.content,
            input.errors,
        );
    }
}

export type QueryInit = {
    categoryId: Id;
    label: string;
    content: string;
};

// Evolution

export type QueryEdit = {
    content: string;
    errors: QueryEvolutionError[];
};

export enum ErrorType {
    ParseError = 'ParseError',
    UpdateWarning = 'UpdateWarning',
    UpdateError = 'UpdateError',
}

export type QueryEvolutionError = {
    type: ErrorType;
    message: string;
    data: unknown;
};

export type QueryDescriptionFromServer = {
    parts: QueryPartDescriptionFromServer[];
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

type QueryPartDescriptionFromServer = {
    datasource: DatasourceFromServer;
    content: string;
    structure: string;
};

export class QueryPartDescription {
    private constructor(
        readonly datasource: Datasource,
        readonly content: string,
        readonly structure: string,
    ) {}

    static fromServer(input: QueryPartDescriptionFromServer): QueryPartDescription {
        return new QueryPartDescription(
            Datasource.fromServer(input.datasource),
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
