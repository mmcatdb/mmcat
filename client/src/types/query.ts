import type { Entity, Id, VersionId } from './id';
import { Signature, type SignatureResponse } from './identifiers';

export type QueryResponse = {
    id: Id;
    version: VersionId;
    lastValid: VersionId;
    categoryId: Id;
    label: string;
    content: string;
    errors: QueryEvolutionError[];
    weight: number | null;
    stats: QueryStats | null;
    index: number;
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
        readonly weight: number | undefined,
        readonly stats: QueryStats | undefined,
        readonly index: number,
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
            input.weight ?? undefined,
            input.stats ?? undefined,
            input.index,
        );
    }

    get finalWeight(): number {
        return this.weight ?? this.stats?.executionCount ?? 0;
    }
}

export type QueryInit = {
    categoryId: Id;
    label: string;
    content: string;
};

export type QueryEdit = {
    label?: string;
    weight?: number;
    isResetWeight?: boolean;
    stats?: QueryStats;
};

// Evolution

export type QueryContentEdit = {
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
    structure: ResultStructureResponse;
    content: string;
};

type ResultStructureResponse = {
    name: string;
    isArray: boolean;
    variable: Variable;
    children: Record<SignatureResponse, ResultStructureResponse>;
};

export class ResultStructure {
    constructor(
        readonly name: string,
        readonly isArray: boolean,
        readonly variable: Variable,
        /** If undefined, this is the root of the tree. */
        readonly signatureFromParent: Signature | undefined,
        readonly children: ResultStructure[],
    ) {}

    static fromResponse(input: ResultStructureResponse, signatureFromParent?: Signature): ResultStructure {
        const children: ResultStructure[] = [];
        for (const signatureResponse in input.children) {
            const signature = Signature.fromResponse(signatureResponse);
            children.push(ResultStructure.fromResponse(input.children[signatureResponse], signature));
        }

        return new ResultStructure(input.name, input.isArray, input.variable, signatureFromParent, children);
    }

    get displayName(): string {
        return this.name + (this.isArray ? '[]' : '');
    }
}

type Variable = {
    name: string;
    isOriginal: boolean;
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

type QueryNodeBase<TType extends QueryNodeType, TData extends object> = {
    type: TType;
    structure: ResultStructureResponse;
} & TData;

export type DatasourceNode = QueryNodeBase<QueryNodeType.Datasource, {
    datasourceIdentifier: string;
    kinds: Record<string, PatternTree>;
    joinCandidates: JoinCandidate[];
    filters: Filter[];
    rootVariable: Variable;
}>;

export type PatternTree = {
    objexKey: number;
    variable: Variable;
    children: Record<SignatureResponse, PatternTree>;
};

export type JoinNode = QueryNodeBase<QueryNodeType.Join, {
    fromChild: QueryNode;
    toChild: QueryNode;
    candidate: JoinCandidate;
}>;

export type JoinCandidate = {
    type: JoinType;
    from: JoinCandidateKind;
    to: JoinCandidateKind;
    variable: Variable;
    recursion: number;
    isOptional: boolean;
};

type JoinCandidateKind = {
    kindName: string;
    datasourceIdentifier: string;
    path: SignatureResponse;
};

enum JoinType {
    IdRef = 'IdRef',
    Value = 'Value',
}

export type FilterNode = QueryNodeBase<QueryNodeType.Filter, {
    child: QueryNode;
    filter: Filter;
}>;

// TODO
type Filter = string;

export type MinusNode = QueryNodeBase<QueryNodeType.Minus, {
    primaryChild: QueryNode;
    minusChild: QueryNode;
}>;

export type OptionalNode = QueryNodeBase<QueryNodeType.Optional, {
    primaryChild: QueryNode;
    optionalChild: QueryNode;
}>;

export type UnionNode = QueryNodeBase<QueryNodeType.Union, {
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
