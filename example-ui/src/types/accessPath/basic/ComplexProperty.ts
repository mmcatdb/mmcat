import { DynamicName, nameFromJSON, Signature, StaticName, type Name, type NameJSON, type SignatureJSON } from "@/types/identifiers";
import { IntendedStringBuilder } from "@/utils/string";
import { subpathFromJSON, type AccessPathJSON } from "./AccessPath";
import type { ChildProperty, ParentProperty } from "./compositeTypes";

export type ComplexPropertyJSON = { _class: 'ComplexProperty', name: NameJSON, signature: SignatureJSON, subpaths: AccessPathJSON[] };

export class ComplexProperty {
    name: Name;
    _signature: Signature;
    parent?: ParentProperty;
    _subpaths: ChildProperty[];
    //_subpaths = new ComparableMap<Signature, string, AccessPath>(signature => signature.toString());

    constructor(name: Name, signature: Signature, parent?: ParentProperty, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
        this._subpaths = [ ...subpaths ];
        //subpaths.forEach(subpath => this._subpaths.set(subpath.signature, subpath));
    }

    static copy(property: ComplexProperty): ComplexProperty {
        const name = property.name instanceof DynamicName ? DynamicName.copy(property.name) : StaticName.copy(property.name);
        return new ComplexProperty(name, property.signature.copy(), property.parent, property.subpaths);
    }

    get isAuxiliary(): boolean {
        return this.signature.isNull;
    }

    get signature(): Signature {
        return this._signature;
    }

    get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        if (!this.isAuxiliary)
            builder.append(this.signature + ' ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
    }

    static fromJSON(jsonObject: ComplexPropertyJSON, parent: ParentProperty): ComplexProperty {
        const property = new ComplexProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.signature),
            parent
        );

        property._subpaths = jsonObject.subpaths.map(subpath => subpathFromJSON(subpath, property));

        return property;
    }

    toJSON(): ComplexPropertyJSON {
        return {
            _class: 'ComplexProperty',
            name: this.name.toJSON(),
            signature: this._signature.toJSON(),
            subpaths: this._subpaths.map(subpath => subpath.toJSON())
        };
    }
}
