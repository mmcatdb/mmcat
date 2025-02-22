import { print, type Printable, type Printer } from '@/types/utils/string';
import { type NameFromServer, nameFromServer, Signature, type SignatureFromServer, type Name } from '@/types/identifiers';
import type { ParentProperty } from './compositeTypes';

export type SimplePropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
};

export class SimpleProperty implements Printable {
    private constructor(
        readonly name: Name,
        readonly signature: Signature,
        public parent?: ParentProperty,
    ) {}

    static fromServer(input: SimplePropertyFromServer, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromServer(input.name),
            Signature.fromServer(input.signature),
            parent,
        );
    }

    toServer(): SimplePropertyFromServer {
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
}
