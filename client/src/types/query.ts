import { type DatasourceFromServer } from './datasource';
import type { Entity, Id, VersionId } from './id';
import { type SignatureFromServer } from './identifiers';

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

export type QueryDescription = {
    planned: QueryPlanDescription;
    optimized: QueryPlanDescription;
};

export type QueryPlanDescription = {
    parts: QueryPartDescription[];
    tree: QueryNode;
};

type QueryPartDescription = {
    datasource: DatasourceFromServer;
    structure: ResultStructure;
    content: string;
};

type ResultStructure = {
    name: string;
    isArray: boolean;
    children: Map<string, ResultStructure>;
    /** If null, this is the root of the tree. */
    parent: ResultStructure | null;
    signatureFromParent: SignatureFromServer | null;
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
    kinds: Record<string, PatternObject>;
    joinCandidates: JoinCandidate[];
    filters: Filter[];
    rootVariable: TODO;
}>;

export type PatternObject = {
    objexKey: number;
    term: string;
    children: Record<SignatureFromServer, PatternObject>;
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
    condition: JoinCondition;
    recursion: number;
    isOptional: boolean;
}

enum JoinType {
    IdRef = 'IdRef',
    Value = 'Value',
}

type JoinCondition = {
    from: SignatureFromServer;
    to: SignatureFromServer;
};


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
