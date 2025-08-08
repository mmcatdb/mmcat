import { print, type Printable, type Printer } from '@/types/utils/string';
import { type Name, nameFromResponse, type NameResponse, Signature } from '@/types/identifiers';
import { ComplexProperty, type ParentProperty, subpathFromResponse, type ChildProperty, type ComplexPropertyResponse } from './ComplexProperty';
import { SimpleProperty } from './SimpleProperty';
import { type AccessPath } from './AccessPath';

// TODO Candice be unified with the ComplexProperty?

export type RootPropertyResponse = ComplexPropertyResponse & { name: NameResponse };

export class RootProperty implements Printable {
    public constructor(
        readonly name: Name,
        readonly subpaths: ChildProperty[],
    ) {}

    readonly isAuxiliary = true;
    readonly signature = Signature.empty();

    static fromResponse(input: RootPropertyResponse): RootProperty {
        const subpaths: ChildProperty[] = [];
        const property = new RootProperty(
            nameFromResponse(input.name),
            subpaths,
        );

        input.subpaths.map(s => subpathFromResponse(s, property)).forEach(s => subpaths.push(s));

        return property;
    }

    toServer(): RootPropertyResponse {
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

    static getPropertyRoot(property: RootProperty | ChildProperty): RootProperty {
        let current: RootProperty | ChildProperty = property;
        while (!(current instanceof RootProperty))
            current = current.parent;

        return current;
    }

    static getPropertyPathFromRoot(property: RootProperty | ChildProperty): Signature {
        const signatures: Signature[] = [];

        let current: RootProperty | ChildProperty = property;
        while (!(current instanceof RootProperty)) {
            signatures.push(current.signature);
            current = current.parent;
        }

        // No need to append the root signature, as it is always empty.
        return Signature.concatenate(...signatures.reverse());
    }

    toEditable(): AccessPath {
        return {
            name: this.name,
            signature: this.signature,
            subpaths: this.subpaths.map(s => s.toEditable()),
            isRoot: true,
        };
    }

    static fromEditable(path: AccessPath): RootProperty {
        if (!path.isRoot)
            throw new Error('Can\'t create root property from non-root access path');

        const subpaths: ChildProperty[] = [];
        const output = new RootProperty(path.name, subpaths);
        path.subpaths.map(s => childPropertyFromEditable(s, output)).forEach(s => subpaths.push(s));

        return output;
    }
}

function childPropertyFromEditable(path: AccessPath, parent: ParentProperty): ChildProperty {
    if (path.isRoot)
        throw new Error('Can\'t create child property from root access path');

    if (path.subpaths.length === 0)
        return new SimpleProperty(path.name, path.signature, parent);

    const subpaths: ChildProperty[] = [];
    const output = new ComplexProperty(path.name, path.signature, parent, subpaths);
    path.subpaths
        .map(s => childPropertyFromEditable(s, output))
        .forEach(s => subpaths.push(s));

    return output;
}
