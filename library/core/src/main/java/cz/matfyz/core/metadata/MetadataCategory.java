package cz.matfyz.core.metadata;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

public class MetadataCategory {

    final SchemaCategory schema;
    private final Map<Key, MetadataObject> objects;
    private final Map<Signature, MetadataMorphism> morphisms;

    MetadataCategory(SchemaCategory schema, Map<Key, MetadataObject> objects, Map<Signature, MetadataMorphism> morphisms) {
        this.schema = schema;
        this.objects = objects;
        this.morphisms = morphisms;
    }

    public static MetadataCategory createEmpty(SchemaCategory schema) {
        return new MetadataCategory(schema, new TreeMap<>(), new TreeMap<>());
    }

    public MetadataObject getObject(SchemaObject object) {
        return objects.get(object.key());
    }

    public MetadataObject getObject(Key key) {
        return objects.get(key);
    }

    public MetadataMorphism getMorphism(SchemaMorphism morphism) {
        return morphisms.get(morphism.signature());
    }

    public MetadataMorphism getMorphism(Signature signature) {
        return morphisms.get(signature);
    }

    public void setObject(SchemaObject object, MetadataObject metadataObject) {
        objects.put(object.key(), metadataObject);
    }

    public void setMorphism(SchemaMorphism morphism, MetadataMorphism metadataMorphism) {
        morphisms.put(morphism.signature(), metadataMorphism);
    }

    public abstract static class Editor {

        protected static Map<Key, MetadataObject> getObjects(MetadataCategory metadata) {
            return metadata.objects;
        }

        protected static Map<Signature, MetadataMorphism> getMorphisms(MetadataCategory metadata) {
            return metadata.morphisms;
        }

    }

}
