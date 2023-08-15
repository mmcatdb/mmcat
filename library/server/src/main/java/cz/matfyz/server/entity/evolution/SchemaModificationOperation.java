package cz.matfyz.server.entity.evolution;

import cz.matfyz.core.schema.Key;
import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author jachym.bartik
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SchemaModificationOperation.CreateObject.class, name = "createObject"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.DeleteObject.class, name = "deleteObject"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.EditObject.class, name = "editObject"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.CreateMorphism.class, name = "createMorphism"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.DeleteMorphism.class, name = "deleteMorphism"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.EditMorphism.class, name = "editMorphism"),
    @JsonSubTypes.Type(value = SchemaModificationOperation.Composite.class, name = "composite"),
})
public interface SchemaModificationOperation {

    public cz.matfyz.evolution.schema.SchemaModificationOperation toEvolution(SchemaCategoryContext context);

    public static record CreateObject(
        Key key,
        SchemaObjectWrapper.Data object
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.CreateObject toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.CreateObject(object.toSchemaObject(key, context));
        }

    }

    public static record DeleteObject(
        Key key,
        SchemaObjectWrapper.Data object
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.DeleteObject toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.DeleteObject(key);
        }

    }

    public static record EditObject(
        Key key,
        SchemaObjectWrapper.Data newObject,
        SchemaObjectWrapper.Data oldObject
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.EditObject toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.EditObject(newObject.toSchemaObject(key, context));
        }

    }

    public static record CreateMorphism(
        SchemaMorphismWrapper morphism
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.CreateMorphism toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.CreateMorphism(
                morphism.toSchemaMorphism(context),
                morphism.domKey(),
                morphism.codKey()
            );
        }

    }

    public static record DeleteMorphism(
        SchemaMorphismWrapper morphism
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.DeleteMorphism toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.DeleteMorphism(
                morphism.signature()
            );
        }
        
    }

    public static record EditMorphism(
        SchemaMorphismWrapper newMorphism,
        SchemaMorphismWrapper oldMorphism
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.EditMorphism toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.EditMorphism(
                newMorphism.toSchemaMorphism(context),
                newMorphism.domKey(),
                newMorphism.codKey()
            );
        }

    }

    public static record Composite(
        String name
    ) implements SchemaModificationOperation {

        @Override
        public cz.matfyz.evolution.schema.Composite toEvolution(SchemaCategoryContext context) {
            return new cz.matfyz.evolution.schema.Composite(
                name
            );
        }

    }

}