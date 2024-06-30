type Bijection<Domain, Codomain> = {
    function: (a: Domain) => Codomain;
    inversion: (to: Codomain) => Domain;
};

const identityFunction = {
    function: (x: number) => x,
    inversion: (x: number) => x,
};

export class UniqueIdProvider<T> {
    private readonly currentValues = new Set<number>();
    private _maxValue = 0;

    constructor(
        private readonly mapping: Bijection<T, number>,
    ) {}

    static identity(): UniqueIdProvider<number> {
        return new UniqueIdProvider(identityFunction);
    }

    add(identifier: T): void {
        const value = this.mapping.function(identifier);
        this.currentValues.add(value);
        this._maxValue = Math.max(this._maxValue, value);
    }

    remove(identifier: T): void {
        const value = this.mapping.function(identifier);
        this.currentValues.delete(value);
    }

    update(newId: T, oldId: T): void {
        this.currentValues.delete(this.mapping.function(oldId));
        this.add(newId);
    }

    suggest(): T {
        return this.mapping.inversion(this._maxValue + 1);
    }

    createAndAdd(): T {
        const identifier = this.suggest();
        this.add(identifier);
        return identifier;
    }

    isAvailable(identifier: T) {
        return !this.currentValues.has(this.mapping.function(identifier));
    }

    get maxValue(): number {
        return this._maxValue;
    }

    get maxIdentifier(): T {
        return this.mapping.inversion(this._maxValue);
    }
}
