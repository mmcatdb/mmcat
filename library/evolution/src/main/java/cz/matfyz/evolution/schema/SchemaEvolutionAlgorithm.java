package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;

import java.util.List;

public class SchemaEvolutionAlgorithm {

    private final Version prevVersion;

    public Version getPrevVersion() {
        return prevVersion;
    }

    public final List<SMO> operations;

    public SchemaEvolutionAlgorithm(Version prevVersion, List<SMO> operations) {
        this.prevVersion = prevVersion;
        this.operations = operations;
    }

    public SchemaCategory up(SchemaCategory schema) {
        for (final var operation : operations)
            operation.up(schema);

        return schema;
    }

    public SchemaCategory down(SchemaCategory schema) {
        for (final var operation : operations.reversed())
            operation.down(schema);

        return schema;
    }

    /**
     * The provided updates are expected to be sorted from the oldest version to the newest.
     */
    public static void setToVersion(SchemaCategory schema, List<SchemaEvolutionAlgorithm> allUpdates, Version currentVersion, Version newVersion) {
        final int comparison = currentVersion.compareTo(newVersion);

        if (comparison < 0) {
            // The current schema category is older than the requested version.
            final int firstIndex = ArrayUtils.indexOf(allUpdates, update -> update.getPrevVersion().equals(currentVersion));
            final int newIndex = ArrayUtils.indexOf(allUpdates, update -> update.getPrevVersion().equals(newVersion));
            // If the update from newVersion to event newer version isn't found, the new version is the newest one.
            // In that case, the last update would have had index equal to the number of all updates.
            final int lastIndex = (newIndex != -1 ? newIndex : allUpdates.size()) - 1;

            for (int i = firstIndex; i < lastIndex; i++) {
                final var update = allUpdates.get(i);
                update.up(schema);
            }
        }
        else if (comparison > 0) {
            // The current schema category is newer than the requested version.
            final int currentIndex = ArrayUtils.indexOf(allUpdates, update -> update.getPrevVersion().equals(currentVersion));
            final int firstIndex = (currentIndex != -1 ? currentIndex : allUpdates.size()) - 1;
            final int lastIndex = ArrayUtils.indexOf(allUpdates, update -> update.getPrevVersion().equals(newVersion));

            for (int i = firstIndex; i > lastIndex; i--) {
                final var update = allUpdates.get(i);
                update.down(schema);
            }
        }
    }

}
