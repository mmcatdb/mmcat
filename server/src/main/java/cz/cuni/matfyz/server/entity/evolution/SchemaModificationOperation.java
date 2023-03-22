package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author jachym.bartik
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AddObject.class, name = "addObject"),
    @JsonSubTypes.Type(value = AddMorphism.class, name = "addMorphism")
})
interface SchemaModificationOperation {

    public cz.cuni.matfyz.evolution.schema.SchemaModificationOperation toEvolution(SchemaCategoryContext context);

}

record AddObject(
    SchemaObjectWrapper object
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.AddObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.AddObject(object.toSchemaObject(context));
    }

}

record AddMorphism(
    SchemaMorphismWrapper morphism
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.AddMorphism toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.AddMorphism(
            morphism.toSchemaMorphism(context),
            morphism.domKey(),
            morphism.codKey()
        );
    }

}
