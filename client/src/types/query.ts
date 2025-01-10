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
    parts: QueryPartDescription[];
    tree: QueryNode;
};

type QueryPartDescription = {
    datasource: DatasourceFromServer;
    structure: QueryStructure;
    content: string;
};

type QueryStructure = {
    name: string;
    isArray: boolean;
    children: Map<string, QueryStructure>;
    /** If null, this is the root of the tree. */
    parent: QueryStructure | null;
    signatureFromParent: SignatureFromServer | null;
};

export enum QueryNodeType {
    Datasource = 'datasource',
    Pattern = 'pattern',
    Join = 'join',
    Filter = 'filter',
    Minus = 'minus',
    Optional = 'optional',
    Union = 'union',
}

export type QueryNode = DatasourceNode | PatternNode | JoinNode | FilterNode | MinusNode | OptionalNode | UnionNode;

type TypedNode<TType extends QueryNodeType, TData extends object> = {
    type: TType;
} & TData;

type TODO = any;

export type DatasourceNode = TypedNode<QueryNodeType.Datasource, {
    child: QueryNode;
    datasourceIdentifier: string;
}>;

export type PatternNode = TypedNode<QueryNodeType.Datasource, {
    kinds: Record<string, PatternObject>;
    joinCandidates: JoinCandidate[];
    rootTerm: TODO;
}>;

export type PatternObject = {
    objexKey: number;
    term: string;
    children: Record<SignatureFromServer, PatternObject>;
};

type JoinCandidate = TODO;

export type JoinNode = TypedNode<QueryNodeType.Join, {
    fromChild: QueryNode;
    toChild: QueryNode;
    candidate: JoinCandidate;
}>;

export type FilterNode = TypedNode<QueryNodeType.Filter, {
    child: QueryNode;
    filter: TODO;
}>;

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
