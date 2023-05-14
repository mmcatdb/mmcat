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
        this.undonedVersions = [];

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

    private undonedVersions: Version[] = [];

    /**
     * Go back through the versions' chain.
     * @param skipLowerLevels If true, all versions on lower levels are treated as one so they will be all applied at the same time.
     * @returns A list of versions which should be down-ed (in given order). If there is no way back, an empty array is returned.
     */
    undo(skipLowerLevels = true): Version[] {
        const output = [ this.currentVersion ];
        let nextVersion = this.currentVersion.parent;

        if (!nextVersion)
            return [];

        if (skipLowerLevels) {
            while (nextVersion.level > this.currentVersion.level) {
                output.push(nextVersion);
                nextVersion = nextVersion.parent;

                if (!nextVersion)
                    return [];
            }
        }

        this.currentVersion = nextVersion;
        this.undonedVersions.push(...output);

        return output;
    }

    /**
     * Go through the undone versions' chain.
     * @param skipLowerLevels If true, all versions on lower levels are treated as one so they will be all applied at the same time.
     * @returns A list of versions which should be upp-ed (in given order).
     */
    redo(skipLowerLevels = true): Version[] {
        if (this.undonedVersions.length === 0)
            return [];

        let lastUndoned = this.undonedVersions.length - 1;
        if (skipLowerLevels) {
            while (lastUndoned >= 0 && this.undonedVersions[lastUndoned].level > this.currentVersion.level)
                lastUndoned--;
        }

        this.currentVersion = this.undonedVersions[lastUndoned];
        const output = this.undonedVersions.slice(lastUndoned).reverse();
        this.undonedVersions = this.undonedVersions.slice(0, lastUndoned);

        return output;
    }

    /**
     * Move from the current version to the target one.
     */
    move(target: Version): { undo: Version[], redo: Version[] } {
        if (target.id === this.currentVersion.id)
            return { undo: [], redo: [] };

        const ancestor = this.findFirstCommonAncestor(target);
        if (!ancestor)
            return { undo: [], redo: [] };

        const sourceToAncestor = [];
        let a = this.currentVersion;
        while (a.id !== ancestor.id) {
            sourceToAncestor.push(a);
            a = a.parent as Version; // A has to have ancestor because it have been found in the previous function.
        }

        const targetToAncestor = [];
        let b = target;
        while (b.id !== ancestor.id) {
            targetToAncestor.push(b);
            b = b.parent as Version; // The same reason as above.
        }

        const redoOutput = targetToAncestor.reverse(); // NOSONAR - It's the last time we use that array, so it's basically just renaming.

        // If it's a straight redo, we annul the undo and redo versions
        for (const redoVersion of redoOutput) {
            const index = this.undonedVersions.length - 1;
            if (index < 0)
                break;

            if (this.undonedVersions[index].id === redoVersion.id) {
                this.undonedVersions.pop();
            }
            else {
                this.undonedVersions = [];
                break;
            }
        }

        this.currentVersion = target;

        return {
            undo: sourceToAncestor,
            redo: redoOutput,
        };
    }

    private findFirstCommonAncestor(target: Version): Version | undefined {
        let a: Version | undefined = this.currentVersion;
        let b: Version | undefined = target;

        const visited: Set<string> = new Set();
        while (a || b) {
            if (a) {
                if (visited.has(a.id))
                    return a;
                visited.add(a.id);
                a = a.parent;
            }

            if (b) {
                if (visited.has(b.id))
                    return b;
                visited.add(b.id);
                b = b.parent;
            }
        }

        return undefined;
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
