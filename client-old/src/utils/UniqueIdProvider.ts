type Bijection<Domain, Codomain> = {
    function: (a: Domain) => Codomain;
    inversion: (to: Codomain) => Domain;
};

const identityFunction = {
    function: (x: number) => x,
    inversion: (x: number) => x,
};

export class UniqueIdProvider<T> {
    _mapping: Bijection<T, number>;
    _currentValues: Set<number> = new Set();
    _maxValue = 0;

    constructor(mapping: Bijection<T, number>) {
        this._mapping = mapping;
    }

    static identity(): UniqueIdProvider<number> {
        return new UniqueIdProvider(identityFunction);
    }

    add(identifier: T): void {
        const value = this._mapping.function(identifier);
        this._currentValues.add(value);
        this._maxValue = Math.max(this._maxValue, value);
    }

    remove(identifier: T): void {
        const value = this._mapping.function(identifier);
        this._currentValues.delete(value);
    }

    update(newId: T, oldId: T): void {
        this._currentValues.delete(this._mapping.function(oldId));
        this.add(newId);
    }

    suggest(): T {
        return this._mapping.inversion(this._maxValue + 1);
    }

    createAndAdd(): T {
        const identifier = this.suggest();
        this.add(identifier);
        return identifier;
    }

    isAvailable(identifier: T) {
        return !this._currentValues.has(this._mapping.function(identifier));
    }

    get maxValue(): number {
        return this._maxValue;
    }

    get maxIdentifier(): T {
        return this._mapping.inversion(this._maxValue);
    }
}
