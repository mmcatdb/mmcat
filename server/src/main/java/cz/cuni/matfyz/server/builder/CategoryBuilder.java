package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

/**
 * @author jachym.bartik
 */
public class CategoryBuilder {

    private SchemaCategoryWrapper categoryWrapper;

    public CategoryBuilder setCategoryWrapper(SchemaCategoryWrapper categoryWrapper) {
        this.categoryWrapper = categoryWrapper;

        return this;
    }

    public SchemaCategory build() {
        final var category = new SchemaCategory(categoryWrapper.label);

        for (final var objectWrapper : categoryWrapper.objects)
            buildObject(objectWrapper, category);

        for (final var morphismWrapper : categoryWrapper.morphisms)
            buildMorphism(morphismWrapper, category);

        return category;
    }

    private void buildObject(SchemaObjectWrapper wrapper, SchemaCategory category) {
        final var object = new SchemaObject(
            wrapper.key(),
            wrapper.label(),
            wrapper.superId(),
            wrapper.ids(),
            wrapper.iri(),
            wrapper.pimIri()
        );

        category.addObject(object);

    }

    private void buildMorphism(SchemaMorphismWrapper wrapper, SchemaCategory category) {
        final var morphism = new SchemaMorphism.Builder().fromArguments(
            wrapper.signature(),
            category.getObject(wrapper.domKey()),
            category.getObject(wrapper.codKey()),
            wrapper.min(),
            wrapper.max(),
            wrapper.label()
        );
            
        category.addMorphism(morphism);
    }

}
