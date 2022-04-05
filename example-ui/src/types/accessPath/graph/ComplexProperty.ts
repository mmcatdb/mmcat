import type { Name, NameJSON, SignatureJSON } from "@/types/identifiers";
import type { AccessPathJSON } from "./AccessPath";
import type { ChildProperty, ParentProperty } from "./compositeTypes";
import type { Node } from "@/types/categoryGraph";
import type { SequenceSignature } from "./SequenceSignature";

export type ComplexPropertyJSON = { _class: 'ComplexProperty', name: NameJSON, signature: SignatureJSON, subpaths: AccessPathJSON[] };

export class ComplexProperty {
    name: Name;
    _signature: SequenceSignature;
    parent: ParentProperty;
    _subpaths: ChildProperty[];
    //_subpaths = new ComparableMap<Signature, string, AccessPath>(signature => signature.toString());

    constructor(name: Name, signature: SequenceSignature, parent: ParentProperty, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
        this._subpaths = [ ...subpaths ];
        //subpaths.forEach(subpath => this._subpaths.set(subpath.signature, subpath));
    }

    /*
    static copy(property: ComplexProperty): ComplexProperty {
        const name = property.name instanceof DynamicName ? DynamicName.copy(property.name) : StaticName.copy(property.name);
        return new ComplexProperty(name, Signature.copy(property.signature), property.parent, property.subpaths);
    }
    */

    update(newName: Name, newSignature: SequenceSignature): void {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature)) {
            this._signature = newSignature;
            //this._subpaths.clear();
            this._subpaths = [];
        }
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
