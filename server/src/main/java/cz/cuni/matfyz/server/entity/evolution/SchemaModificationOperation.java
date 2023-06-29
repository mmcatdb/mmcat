package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.core.schema.Key;
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
    @JsonSubTypes.Type(value = EditObject.class, name = "editObject"),
    @JsonSubTypes.Type(value = CreateMorphism.class, name = "createMorphism"),
    @JsonSubTypes.Type(value = DeleteMorphism.class, name = "deleteMorphism"),
    @JsonSubTypes.Type(value = EditMorphism.class, name = "editMorphism"),
    @JsonSubTypes.Type(value = Composite.class, name = "composite"),
})
interface SchemaModificationOperation {

    public cz.cuni.matfyz.evolution.schema.SchemaModificationOperation toEvolution(SchemaCategoryContext context);

}

record CreateObject(
    Key key,
    SchemaObjectWrapper.Data object
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.CreateObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.CreateObject(object.toSchemaObject(key, context));
    }

}

record DeleteObject(
    Key key,
    SchemaObjectWrapper.Data object
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.DeleteObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.DeleteObject(key);
    }

}

record EditObject(
    Key key,
    SchemaObjectWrapper.Data newObject,
    SchemaObjectWrapper.Data oldObject
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.EditObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.EditObject(newObject.toSchemaObject(key, context));
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

record EditMorphism(
    SchemaMorphismWrapper newMorphism,
    SchemaMorphismWrapper oldMorphism
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.EditMorphism toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.EditMorphism(
            newMorphism.toSchemaMorphism(context),
            newMorphism.domKey(),
            newMorphism.codKey()
        );
    }

}

record Composite(
    String name
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.Composite toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.Composite(
            name
        );
    }

}
