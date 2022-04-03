import { IntendedStringBuilder } from "@/utils/string";
import type { NodeSchemaData } from "../categoryGraph";
import { nameFromJSON, Signature, type Name, type NameJSON, type SignatureJSON } from "../identifiers";
import type { ParentProperty } from "./compositeTypes";

export type SimpleValueJSON = { _class: 'SimpleValue', signature: SignatureJSON };

export type SimplePropertyJSON = { _class: 'SimpleProperty', name: NameJSON, value: SimpleValueJSON };

export class SimpleProperty {
    public name: Name;
    public parent?: ParentProperty;
    private _signature: Signature;
    private _node?: NodeSchemaData;

    public constructor(name: Name, signature: Signature, parent?: ParentProperty) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
    }

    public get signature(): Signature {
        return this._signature;
    }

    public get node(): NodeSchemaData | undefined {
        return this._node;
    }

    public set node(value: NodeSchemaData | undefined) {
        this._node = value;
    }

    public update(newName: Name, newSignature: Signature) {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature))
            this._signature = newSignature;
    }

    public toString(level = 0): string {
        return (level === 0 ? '' : IntendedStringBuilder.getTabIntendationString(level))
            + `${this.name}: ${this._signature}`;
    }

    public static fromJSON(jsonObject: SimplePropertyJSON, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.value.signature),
            parent
        );
    }

    public toJSON(): SimplePropertyJSON {
        return {
            _class: 'SimpleProperty',
            name: this.name.toJSON(),
            value: {
                _class: 'SimpleValue',
                signature: this._signature.toJSON()
            }
        };
    }
}
