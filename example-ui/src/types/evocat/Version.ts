export class Version {
    private children: Version[] = [];

    get firstChild(): Version | undefined {
        return this.children.length === 1 ? this.children[0] : undefined;
    }

    private constructor(
        private readonly branchId: number,
        private readonly levelIds: number[],
        readonly parent?: Version,
    ) {
        this._id = this.branchId + ':' + this.levelIds.join('.');
    }

    private readonly _id;

    get id(): string {
        return this._id;
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
    private readonly versions: Version[];

    private constructor(
        private readonly branchContext: BranchContext,
        private version: Version,
    ) {
        this.versions = [ this.version ];
    }

    get allVersions() {
        return [ ...this.versions ];
    }

    get currentVersion() {
        return this.version;
    }

    set currentVersion(version: Version) {
        this.version = version;
        this.versionListeners.forEach(listener => listener(this.currentVersion));
    }

    nextLevel() {
        this.relativeLevel++;
    }

    prevLevel() {
        this.relativeLevel--;
    }

    createNextVersion(): Version {
        const newVersion = this.version.createChild(this.branchContext, this.relativeLevel);
        this.versions.push(newVersion);
        this.version = newVersion;
        this.relativeLevel = 0;

        this.collectionListeners.forEach(listener => listener(this.allVersions));
        this.versionListeners.forEach(listener => listener(this.currentVersion));

        return newVersion;
    }

    static createNew(): VersionContext {
        return new VersionContext(
            new BranchContext(0),
            Version.createRoot(0, [ 0 ]),
        );
    }

    private collectionListeners: VersionsEventFunction[] = [];
    private versionListeners: VersionEventFunction[] = [];

    addAllListener(listener: VersionsEventFunction) {
        this.collectionListeners.push(listener);
    }

    removeAllListener(listener: VersionsEventFunction) {
        this.collectionListeners = this.collectionListeners.filter(l => l !== listener);
    }

    addCurrentListener(listener: VersionEventFunction) {
        this.versionListeners.push(listener);
    }

    removeCurrentListener(listener: VersionEventFunction) {
        this.versionListeners = this.versionListeners.filter(l => l !== listener);
    }
}

type VersionsEventFunction = (versions: Version[]) => void;
type VersionEventFunction = (version: Version) => void;
