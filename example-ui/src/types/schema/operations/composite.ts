import { type SMO, type SMOFromServer, SMOType } from "./schemaModificationOperation";

export type CompositeFromServer = SMOFromServer<SMOType.Composite> & {
    name: string;
};

export class Composite implements SMO<SMOType.Composite> {
    readonly type = SMOType.Composite;

    private constructor(
        readonly name: string,
    ) {}

    static fromServer(input: CompositeFromServer): Composite {
        return new Composite(
            input.name,
        );
    }

    static create(name: string): Composite {
        return new Composite(
            name,
        );
    }

    toServer(): CompositeFromServer {
        return {
            type: SMOType.Composite,
            name: this.name,
        };
    }

    up(): void {
        /* This function is intentionally empty. */
    }

    down(): void {
        /* This function is intentionally empty. */
    }
}
