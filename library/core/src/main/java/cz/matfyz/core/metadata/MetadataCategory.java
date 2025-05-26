package cz.matfyz.core.metadata;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.Map;
import java.util.TreeMap;

public class MetadataCategory {

    final SchemaCategory schema;
    private final Map<Key, MetadataObjex> objexes;
    private final Map<Signature, MetadataMorphism> morphisms;

    MetadataCategory(SchemaCategory schema, Map<Key, MetadataObjex> objexes, Map<Signature, MetadataMorphism> morphisms) {
        this.schema = schema;
        this.objexes = objexes;
        this.morphisms = morphisms;
    }

    public static MetadataCategory createEmpty(SchemaCategory schema) {
        return new MetadataCategory(schema, new TreeMap<>(), new TreeMap<>());
    }

    public MetadataObjex getObjex(SchemaObjex objex) {
        return objexes.get(objex.key());
    }

    public MetadataObjex getObjex(Key key) {
        return objexes.get(key);
    }

    public MetadataMorphism getMorphism(SchemaMorphism morphism) {
        return morphisms.get(morphism.signature());
    }

    public MetadataMorphism getMorphism(Signature signature) {
        return morphisms.get(signature);
    }

    public void setObjex(SchemaObjex objex, MetadataObjex metadataObjex) {
        objexes.put(objex.key(), metadataObjex);
    }

    public void setMorphism(SchemaMorphism morphism, MetadataMorphism metadataMorphism) {
        morphisms.put(morphism.signature(), metadataMorphism);
    }

    public abstract static class Editor {

        protected static Map<Key, MetadataObjex> getObjexes(MetadataCategory metadata) {
            return metadata.objexes;
        }

        protected static Map<Signature, MetadataMorphism> getMorphisms(MetadataCategory metadata) {
            return metadata.morphisms;
        }

    }

}
