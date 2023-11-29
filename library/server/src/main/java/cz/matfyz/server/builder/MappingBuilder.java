package cz.matfyz.server.builder;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;

import java.util.List;

/**
 * @author jachym.bartik
 */
public class MappingBuilder {

    private SchemaCategoryWrapper categoryWrapper;
    private MappingWrapper mappingWrapper;

    public MappingBuilder setCategoryWrapper(SchemaCategoryWrapper categoryWrapper) {
        this.categoryWrapper = categoryWrapper;

        return this;
    }

    public MappingBuilder setMappingWrapper(MappingWrapper mappingWrapper) {
        this.mappingWrapper = mappingWrapper;

        return this;
    }

    public Mapping build() {
        final var category = categoryWrapper.toSchemaCategory();

        return build(category, mappingWrapper);
    }

    public static Mapping build(SchemaCategory category, MappingWrapper mappingWrapper) {
        return new Mapping(
            category,
            mappingWrapper.rootObjectKey(),
            mappingWrapper.kindName(),
            mappingWrapper.accessPath(),
            List.of(mappingWrapper.primaryKey())
        );
    }

}
