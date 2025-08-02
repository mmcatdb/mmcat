import { print, type Printable, type Printer } from '@/types/utils/string';
import { type NameResponse, nameFromResponse, Signature, type SignatureResponse, type Name } from '@/types/identifiers';
import { type ParentProperty } from './ComplexProperty';
import { type AccessPath } from './AccessPath';

export type SimplePropertyResponse = {
    name: NameResponse;
    signature: SignatureResponse;
};

export class SimpleProperty implements Printable {
    public constructor(
        readonly name: Name,
        readonly signature: Signature,
        readonly parent: ParentProperty,
    ) {}

    static fromResponse(input: SimplePropertyResponse, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromResponse(input.name),
            Signature.fromResponse(input.signature),
            parent,
        );
    }

    toServer(): SimplePropertyResponse {
        return {
            name: this.name.toServer(),
            signature: this.signature.toServer(),
        };
    }

    printTo(printer: Printer): void {
        printer.append(this.name).append(': ').append(this.signature);
    }

    toString(): string {
        return print(this);
    }

    toEditable(): AccessPath {
        return {
            name: this.name,
            signature: this.signature,
            subpaths: [],
            isRoot: false,
        };
    }
}
