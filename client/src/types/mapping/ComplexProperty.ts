import { type NameFromServer, nameFromServer, Signature, type SignatureFromServer, type Name } from '@/types/identifiers';
import { print, type Printable, type Printer } from '@/types/utils/string';
import { SimpleProperty, type SimplePropertyFromServer } from './SimpleProperty';
import { type RootProperty } from './RootProperty';

export type ComplexPropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
    subpaths: ChildPropertyFromServer[];
};

export type ChildPropertyFromServer = ComplexPropertyFromServer | SimplePropertyFromServer;

export type ChildProperty = ComplexProperty | SimpleProperty;

export type ParentProperty = RootProperty | ComplexProperty;

export function subpathFromFromServer(input: ChildPropertyFromServer, parent: ParentProperty): ChildProperty {
    return 'subpaths' in input
        ? ComplexProperty.fromServer(input, parent)
        : SimpleProperty.fromServer(input, parent);
}

export class ComplexProperty implements Printable {
    public constructor(
        readonly name: Name,
        readonly signature: Signature,
        readonly parent: ParentProperty | undefined,
        readonly subpaths: ChildProperty[],
    ) {}

    get isAuxiliary(): boolean {
        return this.signature.isEmpty;
    }

    static fromServer(input: ComplexPropertyFromServer, parent: ParentProperty): ComplexProperty {
        const subpaths: ChildProperty[] = [];
        const property = new ComplexProperty(
            nameFromServer(input.name),
            Signature.fromServer(input.signature),
            parent,
            subpaths,
        );

        input.subpaths.map(subpath => subpathFromFromServer(subpath, property)).forEach(subpath => subpaths.push(subpath));

        return property;
    }

    toServer(): ComplexPropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this.signature.toServer(),
            subpaths: this.subpaths.map(subpath => subpath.toServer()),
        };
    }

    printTo(printer: Printer): void {
        printer.append(this.name).append(': ');
        if (!this.isAuxiliary)
            printer.append(this.signature).append(' ');

        printer.append('{').down().nextLine();

        for (const subpath of this.subpaths)
            printer.append(subpath).append(',').nextLine();

        printer.remove().up().nextLine()
            .append('}');
    }

    toString(): string {
        return print(this);
    }
}
