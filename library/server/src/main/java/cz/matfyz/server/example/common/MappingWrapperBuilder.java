package cz.matfyz.server.example.common;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MappingWrapperBuilder {

    private final List<DatasourceWrapper> datasources;
    private final Id categoryId;
    private final SchemaCategory schema;
    private List<MappingInit> inits = new ArrayList<>();

    public MappingWrapperBuilder(List<DatasourceWrapper> datasources, SchemaCategoryWrapper wrapper) {
        this.datasources = datasources;
        this.categoryId = wrapper.id();
        schema = wrapper.toSchemaCategory();
    }

    public MappingWrapperBuilder add(int index, Function<SchemaCategory, TestMapping> initCreator) {
        return add(datasources.get(index), initCreator);
    }

    public MappingWrapperBuilder add(DatasourceWrapper datasource, Function<SchemaCategory, TestMapping> initCreator) {
        final Mapping mapping = initCreator.apply(schema).mapping();
        final var init = MappingInit.fromMapping(mapping, categoryId, datasource.id());
        inits.add(init);

        return this;
    }

    public List<MappingWrapper> build(Function<MappingInit, MappingWrapper> mappingCreator) {
        return inits.stream().map(mappingCreator).toList();
    }

}
