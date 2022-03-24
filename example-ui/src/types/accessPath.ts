import { nameFromJSON, Signature, type Name, type NameJSON, type SignatureJSON } from "./identifiers";
import { IntendedStringBuilder } from "@/utils/string";

export type AccessPathJSON = SimplePropertyJSON | ComplexPropertyJSON;

export function accessPathFromJSON(jsonObject: AccessPathJSON): AccessPath {
    return jsonObject._class === 'SimpleProperty' ? SimpleProperty.fromJSON(jsonObject) : ComplexProperty.fromJSON(jsonObject);
}

export type AccessPath = SimpleProperty | ComplexProperty;

export type SimpleValueJSON = { _class: 'SimpleValue', signature: SignatureJSON };

export type SimplePropertyJSON = { _class: 'SimpleProperty', name: NameJSON, value: SimpleValueJSON };

export class SimpleProperty {
    public name: Name;
    private value: Signature;

    public constructor(name: Name, value: Signature) {
        this.name = name;
        this.value = value;
    }

    public get signature(): Signature {
        return this.value;
    }

    public toString(level = 0): string {
        return (level === 0 ? '' : IntendedStringBuilder.getTabIntendationString(level))
            + `${this.name}: ${this.value}`;
    }

    public static fromJSON(jsonObject: SimplePropertyJSON): SimpleProperty {
        return new SimpleProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.value.signature)
        )
    }
}

export type ComplexPropertyJSON = { _class: 'ComplexProperty', name: NameJSON, signature: SignatureJSON, subpaths: AccessPathJSON[] };

export class ComplexProperty {
    public name: Name;
    private _signature: Signature;
    private _subpaths: AccessPath[];

    public constructor(name: Name, signature: Signature, subpaths: AccessPath[] = []) {
        this.name = name;
        this._signature = signature;
        this._subpaths = [ ...subpaths ];
    }

    public get isAuxiliary(): boolean {
        return this.signature.isNull;
    }

    public get signature(): Signature {
        return this._signature;
    }

    public get subpaths(): AccessPath[] {
        return this._subpaths;
    }

    public toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        if (!this.isAuxiliary)
            builder.append(this.signature + ' ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString)

        builder.appendIntendedLine('}')

        return builder.toString();
    }

    public static fromJSON(jsonObject: ComplexPropertyJSON): ComplexProperty {
        return new ComplexProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.signature),
            jsonObject.subpaths.map(subpath => accessPathFromJSON(subpath))
        )
    }
}
