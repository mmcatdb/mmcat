import type { Name } from "@/types/identifiers";
import type { GraphChildProperty, GraphParentProperty } from "./compositeTypes";
import type { Node } from "@/types/categoryGraph";
import type { SequenceSignature } from "./SequenceSignature";
import type { ComplexPropertyJSON } from "../JSONTypes";

export class GraphComplexProperty {
    name: Name;
    _signature: SequenceSignature;
    parent: GraphParentProperty;
    _subpaths: GraphChildProperty[];

    constructor(name: Name, signature: SequenceSignature, parent: GraphParentProperty, subpaths: GraphChildProperty[] = []) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
        this._subpaths = [ ...subpaths ];
    }

    update(newName: Name, newSignature: SequenceSignature): void {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature)) {
            this._signature = newSignature;
            this._subpaths = [];
        }
    }

    updateOrAddSubpath(newSubpath: GraphChildProperty, oldSubpath?: GraphChildProperty): void {
        newSubpath.parent = this;
        const index = oldSubpath ? this._subpaths.findIndex(subpath => subpath.signature.equals(oldSubpath.signature)) : -1;
        if (index === -1)
            this._subpaths.push(newSubpath);
        else
            this._subpaths[index] = newSubpath;
    }

    removeSubpath(oldSubpath: GraphChildProperty): void {
        this._subpaths = this._subpaths.filter(subpath => !subpath.signature.equals(oldSubpath.signature));
    }

    get isAuxiliary(): boolean {
        return this.signature.isNull;
    }

    get signature(): SequenceSignature {
        return this._signature;
    }

    get parentNode(): Node {
        return this._signature.sequence.rootNode;
    }

    get node(): Node {
        return this._signature.sequence.lastNode;
    }

    get subpaths(): GraphChildProperty[] {
        return this._subpaths;
    }

    toJSON(): ComplexPropertyJSON {
        return {
            _class: 'ComplexProperty',
            name: this.name.toJSON(),
            signature: this._signature.toSignature().toJSON(),
            subpaths: this._subpaths.map(subpath => subpath.toJSON())
        };
    }
}
