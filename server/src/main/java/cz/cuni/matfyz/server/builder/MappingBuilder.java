package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.service.WrapperService;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        final var category = builder.setCategoryWrapper(categoryWrapper).build();
        final var rootObject = builder.getObject(mappingWrapper.rootObject.id);

        try {
            return new ObjectMapper().readerFor(Mapping.class)
                .withAttribute("category", category)
                .withAttribute("rootObject", rootObject)
                .readValue(mappingWrapper.jsonValue);
        }
        catch (Exception exception) {
            throw new WrapperService.WrapperCreationErrorException(exception.getMessage());
        }
    }

}
