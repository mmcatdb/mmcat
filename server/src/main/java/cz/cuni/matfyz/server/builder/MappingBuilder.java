package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;

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
        final var builder = new CategoryBuilder();
        final var category = builder.setCategoryWrapper(categoryWrapper)
            .build();

        final var rootObject = builder.getObject(mappingWrapper.rootObject.id);
        SchemaMorphism rootMorphism = null;
        
        return new Mapping.Builder().fromJSON(category, rootObject, rootMorphism, mappingWrapper.mappingJsonValue);
    }

}
