import { Version, type VersionFromServer } from '../evocat/Version';
import { fromServer, type SMO, type SMOFromServer } from './operation';

export type VersionedSMOFromServer = {
    version: VersionFromServer;
    smo: SMOFromServer;
};

export class VersionedSMO {
    private constructor(
        readonly version: Version,
        readonly smo: SMO,
        public isNew: boolean,
    ) {}

    static create(version: Version, smo: SMO): VersionedSMO {
        return new VersionedSMO(
            version,
            smo,
            true,
        );
    }

    static fromServer(input: VersionedSMOFromServer): VersionedSMO {
        const version = Version.fromServer(input.version);

        return new VersionedSMO(
            version,
            fromServer(input.smo),
            false,
        );
    }

    toServer(): VersionedSMOFromServer {
        return {
            version: this.version.toServer(),
            smo: this.smo.toServer(),
        };
    }
}
