import type { Graph } from "../categoryGraph";

export class Evocat {
    private constructor(
        readonly graph: Graph,
    ) {}

    static create(graph: Graph): Evocat {
        return new Evocat(
            graph,
        );
    }
}
