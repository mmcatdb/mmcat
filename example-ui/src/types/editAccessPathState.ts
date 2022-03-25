import type { ComplexProperty } from "./accessPath";

export enum StateType {
    Default,
    RootSelected, // TODO
    EditComplexProperty
}

abstract class State {
    public static stateType: StateType;
}

export class Default extends State {
    public static stateType = StateType.Default;
}

export class RootSelected extends State {
    public static stateType = StateType.RootSelected;
}

export class EditComplexProperty extends State {
    public static stateType = StateType.EditComplexProperty;
    public property: ComplexProperty;

    public constructor(property: ComplexProperty) {
        super();
        this.property = property;
    }
}
