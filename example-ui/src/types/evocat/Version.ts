export class Version {
    private children: Version[] = [];

    private constructor(
        private readonly branchId: number,
        private readonly levelIds: number[],
        readonly parent?: Version,
    ) {}

    get id(): string {
        return this.branchId + ':' + this.levelIds.join('.');
    }

    static fromId(id: string, parent?: Version): Version {
        const split = id.split(':');
        const levelIds = split[1].split('.');

        return new Version(
            parseInt(split[0]),
            levelIds.map(parseInt),
            parent,
        );
    }

    toString(): string {
        return this.id;
    }

    get level(): number {
        return this.levelIds.length - 1;
    }

    get levelId(): number {
        return this.levelIds[this.level];
    }

    static createRoot(branchId: number, levelIds: number[]): Version {
        return new Version(branchId, levelIds);
    }

    createChild(context: BranchContext, relativeLevel = 0): Version {
        if (this.level + relativeLevel < 0)
            throw new Error(`Relative level: ${relativeLevel} cannot be applied to version: ${this}.`);

        const newLevelIds = relativeLevel >= 0 ? [ ...this.levelIds ] : this.levelIds.slice(0, this.level + relativeLevel + 1);
        for (let i = 0; i < relativeLevel; i++)
            newLevelIds.push(0);
        newLevelIds[newLevelIds.length - 1]++;

        const child = new Version(
            this.children.length === 0 ? this.branchId : context.generateNextId(),
            newLevelIds,
            this,
        );
        this.children.push(child);

        return child;
    }
}

class BranchContext {
    constructor(
        private maximalId = 0,
    ) {}

    generateNextId(): number {
        this.maximalId++;

        return this.maximalId;
    }
}

export class VersionContext {
    private relativeLevel = 0;

    private constructor(
        private readonly branchContext: BranchContext,
        private currentVersion: Version,
    ) {}

    nextLevel() {
        this.relativeLevel++;
    }

    prevLevel() {
        this.relativeLevel--;
    }

    createNextVersion(): Version {
        this.currentVersion = this.currentVersion.createChild(this.branchContext, this.relativeLevel);
        this.relativeLevel = 0;

        return this.currentVersion;
    }

    static createNew(): VersionContext {
        return new VersionContext(
            new BranchContext(0),
            Version.createRoot(0, [ 0 ]),
        );
    }
}
