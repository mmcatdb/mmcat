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
    @JsonSubTypes.Type(value = CreateObject.class, name = "createObject"),
    @JsonSubTypes.Type(value = DeleteObject.class, name = "deleteObject"),
    @JsonSubTypes.Type(value = CreateMorphism.class, name = "createMorphism"),
    @JsonSubTypes.Type(value = DeleteMorphism.class, name = "deleteMorphism")
})
interface SchemaModificationOperation {

    public cz.cuni.matfyz.evolution.schema.SchemaModificationOperation toEvolution(SchemaCategoryContext context);

}

record CreateObject(
    SchemaObjectWrapper object
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.CreateObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.CreateObject(object.toSchemaObject(context));
    }

}

record DeleteObject(
    SchemaObjectWrapper object
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.DeleteObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.DeleteObject(object.toSchemaObject(context));
    }

}

record CreateMorphism(
    SchemaMorphismWrapper morphism
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.CreateMorphism toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.CreateMorphism(
            morphism.toSchemaMorphism(context),
            morphism.domKey(),
            morphism.codKey()
        );
    }

}

record DeleteMorphism(
    SchemaMorphismWrapper morphism
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.DeleteMorphism toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.DeleteMorphism(
            morphism.toSchemaMorphism(context),
            morphism.domKey(),
            morphism.codKey()
        );
    }

}
