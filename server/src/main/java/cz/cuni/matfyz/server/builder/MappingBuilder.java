package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;

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
        final var context = new SchemaCategoryContext();
        final var category = categoryWrapper.toSchemaCategory(context);

        return build(category, mappingWrapper);
    }

    public static Mapping build(SchemaCategory category, MappingWrapper mappingWrapper) {
        return new Mapping(
            category,
            category.getObject(mappingWrapper.rootObjectKey()),
            mappingWrapper.accessPath(),
            mappingWrapper.kindName(),
            List.of(mappingWrapper.primaryKey())
        );
    }

}
