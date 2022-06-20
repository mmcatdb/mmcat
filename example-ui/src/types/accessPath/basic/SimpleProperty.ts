import { IntendedStringBuilder } from "@/utils/string";
import { nameFromJSON, Signature, type Name, type NameJSON, type SignatureJSON } from "@/types/identifiers";
import type { ParentProperty } from "./compositeTypes";

export type SimpleValueJSON = { signature: SignatureJSON };

export type SimplePropertyJSON = { _class: 'SimpleProperty', name: NameJSON, value: SimpleValueJSON };

export class SimpleProperty {
    name: Name;
    parent?: ParentProperty;
    _signature: Signature;

    constructor(name: Name, signature: Signature, parent?: ParentProperty) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
    }

    get signature(): Signature {
        return this._signature;
    }

    update(newName: Name, newSignature: Signature) {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature))
            this._signature = newSignature;
    }

    toString(level = 0): string {
        return (level === 0 ? '' : IntendedStringBuilder.getTabIntendationString(level))
            + `${this.name}: ${this._signature}`;
    }

    static fromJSON(jsonObject: SimplePropertyJSON, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.value.signature),
            parent
        );
    }

    toJSON(): SimplePropertyJSON {
        return {
            _class: 'SimpleProperty',
            name: this.name.toJSON(),
            value: {
                signature: this._signature.toJSON()
            }
        };
    }
}
