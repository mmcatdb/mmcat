package cz.matfyz.server.example.common;

import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MappingBuilder {
    
    private final List<LogicalModel> models;
    private final SchemaCategory schema;
    private final Version version;
    private List<MappingInit> output = new ArrayList<>();

    public MappingBuilder(List<LogicalModel> models, SchemaCategoryWrapper wrapper) {
        this.models = models;
        schema = wrapper.toSchemaCategory();
        version = wrapper.version;
    }

    public MappingBuilder add(int index, Function<SchemaCategory, TestMapping> creator) {
        return add(models.get(index), creator);
    }

    public MappingBuilder add(LogicalModel model, Function<SchemaCategory, TestMapping> creator) {
        final Mapping mapping = creator.apply(schema).mapping();
        final var init = new MappingInit(
            model.id,
            mapping.rootObject().key(),
            mapping.primaryKey().toArray(Signature[]::new),
            mapping.kindName(),
            mapping.accessPath(),
            version
        );
        output.add(init);

        return this;
    }

    public List<MappingInfo> build(Function<MappingInit, MappingInfo> function) {
        return output.stream().map(function).toList();
    }

}
