type Bijection<Domain, Codomain> = {
    function: (a: Domain) => Codomain;
    inversion: (to: Codomain) => Domain;
}

const identityFunction = {
    function: (x: number) => x,
    inversion: (x: number) => x
};

export class UniqueIdProvider<Identifier> {
    _mapping: Bijection<Identifier, number>
    _currentValues = new Set() as Set<number>;
    _maxValue = 0;

    constructor(mapping: Bijection<Identifier, number>) {
        this._mapping = mapping;
    }

    static identity(): UniqueIdProvider<number> {
        return new UniqueIdProvider(identityFunction);
    }

    add(identifier: Identifier): void {
        const value = this._mapping.function(identifier);
        this._currentValues.add(value);
        this._maxValue = Math.max(this._maxValue, value);
    }

    update(newId: Identifier, oldId: Identifier): void {
        this._currentValues.delete(this._mapping.function(oldId));
        this.add(newId);
    }

    suggest(): Identifier {
        return this._mapping.inversion(this._maxValue + 1);
    }

    createAndAdd(): Identifier {
        const identifier = this.suggest();
        this.add(identifier);
        return identifier;
    }

    isAvailable(identifier: Identifier) {
        return !this._currentValues.has(this._mapping.function(identifier));
    }

    get maxValue(): number {
        return this._maxValue;
    }
}
