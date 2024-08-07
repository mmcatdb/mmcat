package cz.matfyz.server.example.common;

import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MappingBuilder {

    private final List<LogicalModel> models;
    private final SchemaCategory schema;
    private List<MappingInit> inits = new ArrayList<>();

    public MappingBuilder(List<LogicalModel> models, SchemaCategoryWrapper wrapper) {
        this.models = models;
        schema = wrapper.toSchemaCategory();
    }

    public MappingBuilder add(int index, Function<SchemaCategory, TestMapping> initCreator) {
        return add(models.get(index), initCreator);
    }

    public MappingBuilder add(LogicalModel model, Function<SchemaCategory, TestMapping> initCreator) {
        final Mapping mapping = initCreator.apply(schema).mapping();
        final var init = MappingInit.fromMapping(mapping, model.id());
        inits.add(init);

        return this;
    }

    public List<MappingInfo> build(Function<MappingInit, MappingInfo> mappingCreator) {
        return inits.stream().map(mappingCreator).toList();
    }

}
