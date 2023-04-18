package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.core.category.Signature;
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
    @JsonSubTypes.Type(value = AddObject.class, name = "addObject"),
    @JsonSubTypes.Type(value = DeleteObject.class, name = "deleteObject"),
    @JsonSubTypes.Type(value = AddMorphism.class, name = "addMorphism"),
    @JsonSubTypes.Type(value = DeleteMorphism.class, name = "deleteMorphism")
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

record DeleteObject(
    Key key
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.DeleteObject toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.DeleteObject(key);
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

record DeleteMorphism(
    Signature signature
) implements SchemaModificationOperation {

    @Override
    public cz.cuni.matfyz.evolution.schema.DeleteMorphism toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.DeleteMorphism(signature);
    }

}
