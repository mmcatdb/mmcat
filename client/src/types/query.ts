import type { Entity, Id, VersionId } from './id';
import { type SignatureResponse } from './identifiers';

export type QueryResponse = {
    id: Id;
    version: VersionId;
    lastValid: VersionId;
    categoryId: Id;
    label: string;
    content: string;
    errors: QueryEvolutionError[];
    stats: QueryStats | null;
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
        readonly stats: QueryStats | undefined,
    ) {}

    static fromResponse(input: QueryResponse): Query {
        return new Query(
            input.id,
            input.version,
            input.lastValid,
            input.categoryId,
            input.label,
            input.content,
            input.errors,
            input.stats ?? undefined,
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
    /**
     * If true, ve should forget the query history.
     * Basically the new version is too different for the old stats to make sense.
     */
    isResetStats: boolean;
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

// Querying

export type QueryResult = {
    rows: string[];
    stats: QueryStats | null;
};

export type QueryDescription = {
    planned: QueryPlanDescription;
    optimized: QueryPlanDescription;
};

type QueryPlanDescription = {
    parts: QueryPartDescription[];
    tree: QueryNode;
};

export type QueryPartDescription = {
    datasourceIdentifier: Id;
    structure: ResultStructure;
    content: string;
};

type ResultStructure = {
    name: string;
    isArray: boolean;
    children: Map<string, ResultStructure>;
    /** If null, this is the root of the tree. */
    parent: ResultStructure | null;
    signatureFromParent: SignatureResponse | null;
};

export enum QueryNodeType {
    Datasource = 'datasource',
    Join = 'join',
    Filter = 'filter',
    Minus = 'minus',
    Optional = 'optional',
    Union = 'union',
}

export type QueryNode = DatasourceNode | JoinNode | FilterNode | MinusNode | OptionalNode | UnionNode;

type TypedNode<TType extends QueryNodeType, TData extends object> = {
    type: TType;
} & TData;

type TODO = any;

export type DatasourceNode = TypedNode<QueryNodeType.Datasource, {
    datasourceIdentifier: string;
    kinds: Record<string, PatternTree>;
    joinCandidates: JoinCandidate[];
    filters: Filter[];
    rootVariable: TODO;
}>;

export type PatternTree = {
    objexKey: number;
    term: string;
    children: Record<SignatureResponse, PatternTree>;
};

export type JoinNode = TypedNode<QueryNodeType.Join, {
    fromChild: QueryNode;
    toChild: QueryNode;
    candidate: JoinCandidate;
}>;

export type JoinCandidate = {
    type: JoinType;
    fromKind: string;
    toKind: string;
    variable: TODO;
    fromPath: SignatureResponse;
    toPath: SignatureResponse;
    recursion: number;
    isOptional: boolean;
};

enum JoinType {
    IdRef = 'IdRef',
    Value = 'Value',
}

export type FilterNode = TypedNode<QueryNodeType.Filter, {
    child: QueryNode;
    filter: Filter;
}>;

// TODO
type Filter = string;

export type MinusNode = TypedNode<QueryNodeType.Minus, {
    primaryChild: QueryNode;
    minusChild: QueryNode;
}>;

export type OptionalNode = TypedNode<QueryNodeType.Optional, {
    primaryChild: QueryNode;
    optionalChild: QueryNode;
}>;

export type UnionNode = TypedNode<QueryNodeType.Union, {
    children: QueryNode[];
}>;

// Stats

export type QueryStats = {
    executionCount: number;
    resultSizeInBytes: AggregatedNumber;
    planningTimeInMs: AggregatedNumber;
    evaluationTimeInMs: AggregatedNumber;
};

export type AggregatedNumber = {
    min: number;
    max: number;
    sum: number;
};
