import type { Iri } from '@/types/integration/';
import { Key, Signature, type KeyFromServer, type SignatureFromServer } from '../identifiers';
import type { SchemaObject } from './SchemaObject';
import type { Optional } from '@/utils/common';
import type { Graph } from '../categoryGraph';

export type SchemaMorphismFromServer = {
    signature: SignatureFromServer;
    label?: string;
    domKey: KeyFromServer;
    codKey: KeyFromServer;
    min: Min;
    iri?: Iri;
    pimIri?: Iri;
    tags?: Tag[];
};

export enum Cardinality {
    Zero = 'ZERO',
    One = 'ONE',
    Star = 'STAR'
}

export type Min = Cardinality.Zero | Cardinality.One;
export type Max = Cardinality.One | Cardinality.Star;

export enum Tag {
    Isa = 'isa',
    Role = 'role'
}

export class SchemaMorphism {
    private constructor(
        readonly signature: Signature,
        readonly domKey: Key,
        readonly codKey: Key,
        readonly min: Min,
        readonly label: string,
        readonly tags: Tag[],
        readonly iri: Iri | undefined,
        readonly pimIri: Iri | undefined,
        private _isNew: boolean,
    ) {}

    static fromServer(input: SchemaMorphismFromServer): SchemaMorphism {
        return new SchemaMorphism(
            Signature.fromServer(input.signature),
            Key.fromServer(input.domKey),
            Key.fromServer(input.codKey),
            input.min,
            input.label ?? '',
            input.tags ? input.tags : [],
            input.iri,
            input.pimIri,
            false,
        );
    }

    static createNew(signature: Signature, def: MorphismDefinition): SchemaMorphism {
        const [ iri, pimIri ] = 'iri' in def
            ? [ def.iri, def.pimIri ]
            : [ undefined, undefined ];

        return new SchemaMorphism(
            signature,
            def.dom.key,
            def.cod.key,
            def.min,
            def.label ?? '',
            def.tags ?? [],
            iri,
            pimIri,
            true,
        );
    }

    createCopy(def: MorphismDefinition): SchemaMorphism {
        return SchemaMorphism.createNew(this.signature, def);
    }

    toServer(): SchemaMorphismFromServer {
        return {
            signature: this.signature.toServer(),
            domKey: this.domKey.toServer(),
            codKey: this.codKey.toServer(),
            min: this.min,
            label: this.label,
            iri: this.iri,
            pimIri: this.pimIri,
            tags: this.tags,
        };
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get isBase(): boolean {
        return this.signature.isBase;
    }

    get sortBaseValue(): number {
        const baseValue = this.signature.baseValue;
        return Math.abs(baseValue ? baseValue : 0);
    }

    equals(other: SchemaMorphism | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

export type MorphismDefinition = {
    dom: SchemaObject;
    cod: SchemaObject;
    min: Min;
    label?: string;
    tags?: Tag[];
} & Optional<{
    iri: Iri;
    pimIri: Iri;
}>;

export class VersionedSchemaMorphism {
    private constructor(
        readonly signature: Signature,
        private _graph?: Graph,
    ) {}

    static create(signature: Signature, graph: Graph | undefined): VersionedSchemaMorphism {
        return new VersionedSchemaMorphism(
            signature,
            graph,
        );
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;
        if (!newGraph)
            return;

        this.updateGraph(newGraph);
    }

    private _current?: SchemaMorphism;

    get current(): SchemaMorphism | undefined {
        return this._current;
    }

    set current(value: SchemaMorphism | undefined) {
        this._current = value;
        if (this._graph)
            this.updateGraph(this._graph);
    }

    private updateGraph(graph: Graph) {
        console.log('UPDATE');
        graph.deleteEdge(this.signature);
        if (this._current)
            graph.createEdge(this._current);
    }
}
