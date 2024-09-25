package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record DatasourceDetail(
    Id id,
    DatasourceType type,
    String label,
    ObjectNode settings,
    DatasourceConfiguration configuration
) {

    private static DatasourceDetail createFromConfiguration(DatasourceWrapper datasource, DatasourceConfiguration configuration) {
        return new DatasourceDetail(
            datasource.id(),
            datasource.type,
            datasource.label,
            // Don't forget to sanitize the settings.
            datasource.getSanitizedSettings(),
            configuration
        );
    }

    public static DatasourceDetail create(DatasourceWrapper datasource, AbstractControlWrapper wrapper) {
        final var configuration = DatasourceConfiguration.create(datasource, wrapper);
        return createFromConfiguration(datasource, configuration);
    }

    public record DatasourceConfiguration(
        boolean isPropertyToOneAllowed,
        boolean isPropertyToManyAllowed,
        boolean isInliningToOneAllowed,
        boolean isInliningToManyAllowed,
        boolean isGroupingAllowed,
        boolean isDynamicNamingAllowed,
        boolean isAnonymousNamingAllowed,
        boolean isReferenceAllowed,
        boolean isComplexPropertyAllowed,
        boolean isSchemaless,
        boolean isWritable,
        boolean isQueryable
    ) {

        public static DatasourceConfiguration create(DatasourceWrapper datasource, AbstractControlWrapper wrapper) {
            final var path = wrapper.getPathWrapper();

            return new DatasourceConfiguration(
                path.isPropertyToOneAllowed(),
                path.isPropertyToManyAllowed(),
                path.isInliningToOneAllowed(),
                path.isInliningToManyAllowed(),
                path.isGroupingAllowed(),
                path.isDynamicNamingAllowed(),
                path.isAnonymousNamingAllowed(),
                path.isReferenceAllowed(),
                path.isComplexPropertyAllowed(),
                path.isSchemaless(),
                wrapper.isWritable(),
                wrapper.isQueryable()
            );
        }

    }

}

