import { type NameResponse, nameFromResponse, Signature, type SignatureResponse, type Name, DynamicName, TypedName, IndexName } from '@/types/identifiers';
import { print, type Printable, type Printer } from '@/types/utils/string';
import { SimpleProperty, type SimplePropertyResponse } from './SimpleProperty';
import { type RootProperty } from './RootProperty';
import { type AccessPath } from './AccessPath';

export type ComplexPropertyResponse = {
    name: NameResponse;
    signature: SignatureResponse;
    subpaths: AccessPathResponse[];
};

export type AccessPathResponse = ComplexPropertyResponse | SimplePropertyResponse;

export type ChildProperty = ComplexProperty | SimpleProperty;

export type ParentProperty = RootProperty | ComplexProperty;

export function subpathFromResponse(input: AccessPathResponse, parent: ParentProperty): ChildProperty {
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

    getTypedSubpath(type: string): ChildProperty {
        return this.subpaths.find(s => s.name instanceof TypedName && s.name.type === type)!;
    }

    getIndexSubpaths(): SimpleProperty[] {
        return this.subpaths
            .filter(s => s.name instanceof IndexName)
            .sort((a, b) => (a.name as IndexName).dimension - (b.name as IndexName).dimension) as SimpleProperty[];
    }

    printTo(printer: Printer): void {
        if (!printer.context(PRINTER_CONTEXT_SHORT_FORM)) {
            // A default option - just print the full property.
            printer
                .append(this.name).append(': ')
                .append(this.signature).append(' ');

            this.printBody(printer);
            return;
        }

        if (this.name instanceof DynamicName) {
            const key = this.getTypedSubpath(TypedName.KEY);
            const value = this.getTypedSubpath(TypedName.VALUE);

            printer
                .append('<').append(this.name.pattern ?? '').append('>: ')
                .append(this.signature)
                .append(' <').append(key.signature).append('> ')
                .append(value);
            return;
        }

        if (!(this.name instanceof TypedName && this.name.type === TypedName.VALUE))
            printer.append(this.name).append(': ');

        printer.append(this.signature).append(' ');

        const indexes = this.getIndexSubpaths();
        if (indexes.length > 0) {
            const value = this.getTypedSubpath(TypedName.VALUE);
            indexes.forEach(index => {
                printer.append('[').append(index.signature).append(']');
            });
            printer.append(' ').append(value);
            return;
        }

        this.printBody(printer);
    }

    private printBody(printer: Printer): void {
        printer.append('{').down().nextLine();

        for (const subpath of this.subpaths)
            printer.append(subpath).append(',').nextLine();

        printer.remove().up().nextLine()
            .append('}');
    }

    toString(isShortForm = false): string {
        return print(this, {
            [PRINTER_CONTEXT_SHORT_FORM]: isShortForm,
        });
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

export const PRINTER_CONTEXT_SHORT_FORM = Symbol('ComplexPropertyShortForm');
