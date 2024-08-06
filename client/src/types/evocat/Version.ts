export type VersionFromServer = string;

export class Version {
    private children: Version[] = [];

    get firstChild(): Version | undefined {
        return this.children.length > 0 ? this.children[0] : undefined;
    }

    get lastChild(): Version | undefined {
        return this.children.length > 0 ? this.children[this.children.length - 1] : undefined;
    }

    private constructor(
        private readonly levelIds: number[],
        private _parent?: Version,
    ) {
        this._id = this.levelIds.join('.');
    }

    static createRoot(levelIds: number[]): Version {
        return new Version(levelIds);
    }

    get parent(): Version | undefined {
        return this._parent;
    }

    set parent(newValue: Version | undefined) {
        if (this._parent) {
            const index = this._parent.children.indexOf(this);
            if (index !== -1)
                this._parent.children.splice(index, 1);

            this._parent = undefined;
        }

        if (newValue) {
            this._parent = newValue;
            this._parent.children.push(this);
        }
    }

    createChild(relativeLevel = 0): Version {
        if (this.level + relativeLevel < 0)
            throw new Error(`Relative level: ${relativeLevel} cannot be applied to version: ${this}.`);

        const newLevelIds = relativeLevel >= 0 ? [ ...this.levelIds ] : this.levelIds.slice(0, this.level + relativeLevel + 1);
        for (let i = 0; i < relativeLevel; i++)
            newLevelIds.push(0);
        newLevelIds[newLevelIds.length - 1]++;

        const child = new Version(
            newLevelIds,
            this,
        );
        this.children.push(child);

        return child;
    }

    static fromServer(input: VersionFromServer, parent?: Version): Version {
        const levelIds = input.split('.');

        const output = new Version(
            levelIds.map(id => parseInt(id)),
        );

        // Let's make sure the version is properly added to the parent.
        output.parent = parent;
        return output;
    }

    toServer(): VersionFromServer {
        return this._id;
    }

    private readonly _id;

    get id(): string {
        return this._id;
    }

    toString(): string {
        return this.id;
    }

    /** Returns a number < 0 if this version is smaller (i.e., it was created sooner) than the other version. */
    compare(other: Version): number {
        const minLength = Math.min(this.levelIds.length, other.levelIds.length);
        for (let i = 0; i < minLength; i++) {
            const comparison = this.levelIds[i] - other.levelIds[i];
            if (comparison !== 0)
                return comparison;
        }

        return this.levelIds.length - other.levelIds.length;
    }

    equals(other: Version): boolean {
        return this._id === other._id;
    }

    get level(): number {
        return this.levelIds.length - 1;
    }

    get levelId(): number {
        return this.levelIds[this.level];
    }

    get isCompositeWrapper(): boolean {
        return !!this._parent && this._parent.level > this.level;
    }
}

export class VersionContext {
    private relativeLevel = 0;

    private constructor(
        private versions: Version[],
        private version: Version,
        private lastSavedVersion: Version,
    ) {}

    static create(versions: Version[]) {
        const root = Version.createRoot([ 0 ]);

        const lastVersion = versions.length === 0
            ? root
            : versions[versions.length - 1];

        const allVersions = [ root, ...versions ];
        for (let i = 0; i < allVersions.length - 1; i++)
            allVersions[i + 1].parent = allVersions[i];

        return new VersionContext(
            allVersions,
            lastVersion,
            lastVersion,
        );
    }

    get allVersions() {
        return [ ...this.versions ];
    }

    get root() {
        return this.versions[0];
    }

    get currentVersion() {
        return this.version;
    }

    set currentVersion(version: Version) {
        this.version = version;
        this.versionListeners.forEach(listener => listener(this.currentVersion));
    }

    nextLevel() {
        if (this.lastSavedVersion.compare(this.currentVersion) > 0)
            throw new Error(`Can't alter previous versions. Current version: ${this.currentVersion}, last saved version: ${this.lastSavedVersion}.`);

        this.relativeLevel++;
    }

    prevLevel() {
        if (this.lastSavedVersion.compare(this.currentVersion) > 0)
            throw new Error(`Can't alter previous versions. Current version: ${this.currentVersion}, last saved version: ${this.lastSavedVersion}.`);

        this.relativeLevel--;
    }

    createNextVersion(): Version {
        if (this.lastSavedVersion.compare(this.currentVersion) > 0)
            throw new Error(`Can't alter previous versions. Current version: ${this.currentVersion}, last saved version: ${this.lastSavedVersion}.`);

        const newVersion = this.version.createChild(this.relativeLevel);
        this.versions.push(newVersion);
        this.version = newVersion;
        this.relativeLevel = 0;
        this.undonedVersions = [];

        this.collectionListeners.forEach(listener => listener(this.allVersions));
        this.versionListeners.forEach(listener => listener(this.currentVersion));

        return newVersion;
    }

    removeVersions(versionsToRemove: Version[]) {
        versionsToRemove.forEach(version => version.parent = undefined);
        this.versions = [ this.root, ...this.versions.filter(version => version.parent) ];

        if (versionsToRemove.includes(this.version)) {
            this.version = this.versions[this.versions.length - 1];
            this.versionListeners.forEach(listener => listener(this.currentVersion));
        }

        this.collectionListeners.forEach(listener => listener(this.allVersions));
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
            while (lastUndoned > 0 && this.undonedVersions[lastUndoned].level > this.currentVersion.level)
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
        const source = this.currentVersion;
        const comparison = source.compare(target);
        if (comparison === 0)
            return { undo: [], redo: [] };

        // If the source version is after the target one, we have to undo the versions.
        const isUndo = comparison > 0;
        if (isUndo) {
            const undo = VersionContext.findPathFromChild(target, source);
            this.undonedVersions.push(...undo);

            this.currentVersion = target;
            return { undo, redo: [] };
        }

        const targetToSource = VersionContext.findPathFromChild(source, target);
        const redo = targetToSource.reverse(); // NOSONAR
        for (const version of redo) {
            const index = this.undonedVersions.length - 1;
            if (index < 0)
                break;

            if (this.undonedVersions[index].id === version.id) {
                this.undonedVersions.pop();
            }
            else {
                this.undonedVersions = [];
                break;
            }
        }

        this.currentVersion = target;
        return { undo: [], redo };
    }

    /** Make sure the ancestor is truly an ancestor of the child. */
    private static findPathFromChild(ancestor: Version, child: Version): Version[] {
        const path = [];
        while (child.id !== ancestor.id) {
            path.push(child);
            child = child.parent!;
        }
        return path;
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

export function computeLatestVersions(rootVersion: Version) {
    const output: Version[] = [];
    let nextVersion: Version | undefined = rootVersion;

    while (nextVersion) {
        output.push(nextVersion);
        nextVersion = nextVersion.lastChild;
    }

    return output;
}
