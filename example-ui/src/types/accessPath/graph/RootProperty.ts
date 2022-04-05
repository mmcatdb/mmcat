import type { Node } from "@/types/categoryGraph";
import type { Name } from "@/types/identifiers";
import type { ComplexPropertyJSON } from "./ComplexProperty";
import type { ChildProperty } from "./compositeTypes";
import { SequenceSignature } from "./SequenceSignature";

export class RootProperty {
    name: Name; // TODO should be static name
    _subpaths: ChildProperty[];
    _signature: SequenceSignature;

    constructor(name: Name, rootNode: Node, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
        this._signature = SequenceSignature.null(rootNode);
    }

    update(newName: Name): void {
        if (!this.name.equals(newName))
            this.name = newName;
    }

    updateOrAddSubpath(newSubpath: ChildProperty, oldSubpath?: ChildProperty): void {
        newSubpath.parent = this;
        const index = oldSubpath ? this._subpaths.findIndex(subpath => subpath.signature.equals(oldSubpath.signature)) : -1;
        if (index === -1)
            this._subpaths.push(newSubpath);
        else
            this._subpaths[index] = newSubpath;
    }

    get isAuxiliary(): boolean {
        return true;
    }

    get signature(): SequenceSignature {
        return this._signature;
    }

    get node(): Node {
        return this._signature.sequence.lastNode;
    }

    get subpaths(): ChildProperty[] {
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
