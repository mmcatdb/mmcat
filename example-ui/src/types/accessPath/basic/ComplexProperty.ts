import { DynamicName, nameFromJSON, Signature, StaticName, type Name } from "@/types/identifiers";
import { IntendedStringBuilder } from "@/utils/string";
import type { ComplexPropertyJSON } from "../JSONTypes";
import { subpathFromJSON, type ChildProperty, type ParentProperty } from "./compositeTypes";

export class ComplexProperty {
    name: Name;
    _signature: Signature;
    _isAuxiliary: boolean;
    parent?: ParentProperty;
    _subpaths: ChildProperty[];

    private constructor(name: Name, signature: Signature, isAuxiliary: boolean, parent?: ParentProperty, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._signature = signature;
        this._isAuxiliary = isAuxiliary;
        this.parent = parent;
        this._subpaths = [ ...subpaths ];
    }

    static copy(property: ComplexProperty): ComplexProperty {
        const name = property.name instanceof DynamicName ? DynamicName.copy(property.name) : StaticName.copy(property.name);
        return new ComplexProperty(name, property.signature.copy(), property._isAuxiliary, property.parent, property.subpaths);
    }

    get isAuxiliary(): boolean {
        return this._isAuxiliary;
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
            jsonObject.isAuxiliary,
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
            isAuxiliary: this._isAuxiliary,
            subpaths: this._subpaths.map(subpath => subpath.toJSON())
        };
    }
}
