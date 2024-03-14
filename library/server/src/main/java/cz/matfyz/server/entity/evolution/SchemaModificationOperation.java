package cz.matfyz.server.entity.evolution;

import cz.matfyz.core.identifiers.Key;
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

    cz.matfyz.evolution.schema.SchemaModificationOperation toEvolution();

    public record CreateObject(
        Key key,
        SchemaObjectWrapper.Data object
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.CreateObject toEvolution() {
            return new cz.matfyz.evolution.schema.CreateObject(
                object.toSchemaObject(key)
            );
        }

    }

    public record DeleteObject(
        Key key,
        SchemaObjectWrapper.Data object
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.DeleteObject toEvolution() {
            return new cz.matfyz.evolution.schema.DeleteObject(
                object.toSchemaObject(key)
            );
        }

    }

    public record EditObject(
        Key key,
        SchemaObjectWrapper.Data newObject,
        SchemaObjectWrapper.Data oldObject
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.EditObject toEvolution() {
            return new cz.matfyz.evolution.schema.EditObject(
                newObject.toSchemaObject(key),
                oldObject.toSchemaObject(key)
            );
        }

    }

    public record CreateMorphism(
        SchemaMorphismWrapper morphism
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.CreateMorphism toEvolution() {
            return new cz.matfyz.evolution.schema.CreateMorphism(
                morphism.toDisconnectedSchemaMorphism()
            );
        }

    }

    public record DeleteMorphism(
        SchemaMorphismWrapper morphism
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.DeleteMorphism toEvolution() {
            return new cz.matfyz.evolution.schema.DeleteMorphism(
                morphism.toDisconnectedSchemaMorphism()
            );
        }

    }

    public record EditMorphism(
        SchemaMorphismWrapper newMorphism,
        SchemaMorphismWrapper oldMorphism
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.EditMorphism toEvolution() {
            return new cz.matfyz.evolution.schema.EditMorphism(
                newMorphism.toDisconnectedSchemaMorphism(),
                oldMorphism.toDisconnectedSchemaMorphism()
            );
        }

    }

    public record Composite(
        String name
    ) implements SchemaModificationOperation {

        @Override public cz.matfyz.evolution.schema.Composite toEvolution() {
            return new cz.matfyz.evolution.schema.Composite(
                name
            );
        }

    }

}
