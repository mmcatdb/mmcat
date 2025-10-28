import { type NameResponse, nameFromResponse, Signature, type SignatureResponse, type Name } from '@/types/identifiers';
import { print, type Printable, type Printer } from '@/types/utils/string';
import { SimpleProperty, type SimplePropertyResponse } from './SimpleProperty';
import { type RootProperty } from './RootProperty';
import { type AccessPath } from './AccessPath';

export type ComplexPropertyResponse = {
    name: NameResponse;
    signature: SignatureResponse;
    subpaths: ChildPropertyResponse[];
};

export type ChildPropertyResponse = ComplexPropertyResponse | SimplePropertyResponse;

export type ChildProperty = ComplexProperty | SimpleProperty;

export type ParentProperty = RootProperty | ComplexProperty;

export function subpathFromResponse(input: ChildPropertyResponse, parent: ParentProperty): ChildProperty {
    return 'subpaths' in input
        ? ComplexProperty.fromResponse(input, parent)
        : SimpleProperty.fromResponse(input, parent);
}

export class ComplexProperty implements Printable {
    public constructor(
        readonly name: Name,
        readonly signature: Signature,
        readonly parent: ParentProperty,
        readonly subpaths: ChildProperty[],
    ) {}

    get isAuxiliary(): boolean {
        return this.signature.isEmpty;
    }

    static fromResponse(input: ComplexPropertyResponse, parent: ParentProperty): ComplexProperty {
        const subpaths: ChildProperty[] = [];
        const property = new ComplexProperty(
            nameFromResponse(input.name),
            Signature.fromResponse(input.signature),
            parent,
            subpaths,
        );

        input.subpaths.map(s => subpathFromResponse(s, property)).forEach(s => subpaths.push(s));

        return property;
    }

    toServer(): ComplexPropertyResponse {
        return {
            name: this.name.toServer(),
            signature: this.signature.toServer(),
            subpaths: this.subpaths.map(s => s.toServer()),
        };
    }

    printTo(printer: Printer): void {
        printer
            .append(this.name).append(': ')
            .append(this.signature).append(' ');

        printer.append('{').down().nextLine();

        for (const subpath of this.subpaths)
            printer.append(subpath).append(',').nextLine();

        printer.remove().up().nextLine()
            .append('}');
    }

    toString(): string {
        return print(this);
    }

    toEditable(): AccessPath {
        return {
            name: this.name,
            signature: this.signature,
            subpaths: this.subpaths.map(s => s.toEditable()),
            isRoot: false,
        };
    }
}
