import type { Node } from "@/types/categoryGraph";
import type { Name, NameJSON, SignatureJSON } from "@/types/identifiers";
import type { ParentProperty } from "./compositeTypes";
import type { SequenceSignature } from "./SequenceSignature";

export type SimpleValueJSON = { signature: SignatureJSON };

export type SimplePropertyJSON = { _class: 'SimpleProperty', name: NameJSON, value: SimpleValueJSON };

export class SimpleProperty {
    name: Name;
    _signature: SequenceSignature;
    parent: ParentProperty;

    constructor(name: Name, signature: SequenceSignature, parent: ParentProperty) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
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

    update(newName: Name, newSignature: SequenceSignature) {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature))
            this._signature = newSignature;
    }

    toJSON(): SimplePropertyJSON {
        return {
            _class: 'SimpleProperty',
            name: this.name.toJSON(),
            value: {
                signature: this._signature.toSignature().toJSON()
            }
        };
    }
}
