package cz.matfyz.server.example.common;

import cz.matfyz.server.controller.MappingController.MappingInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.MappingEntity;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MappingEntityBuilder {

    private final List<DatasourceEntity> datasources;
    private final Id categoryId;
    private final SchemaCategory schema;
    private List<MappingInit> inits = new ArrayList<>();

    public MappingEntityBuilder(List<DatasourceEntity> datasources, SchemaCategoryEntity categoryEntity) {
        this.datasources = datasources;
        this.categoryId = categoryEntity.id();
        schema = categoryEntity.toSchemaCategory();
    }

    public MappingEntityBuilder add(int index, Function<SchemaCategory, TestMapping> initCreator) {
        return add(datasources.get(index), initCreator);
    }

    public MappingEntityBuilder add(DatasourceEntity datasource, Function<SchemaCategory, TestMapping> initCreator) {
        final Mapping mapping = initCreator.apply(schema).mapping();
        final var init = MappingInit.fromMapping(mapping, categoryId, datasource.id());
        inits.add(init);

        return this;
    }

    public List<MappingEntity> build(Function<MappingInit, MappingEntity> mappingCreator) {
        return inits.stream().map(mappingCreator).toList();
    }

}
