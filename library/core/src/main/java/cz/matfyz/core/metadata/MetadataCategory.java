package cz.matfyz.core.metadata;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MetadataCategory {

    final SchemaCategory schema;
    private final Map<Key, MetadataObjex> objexes;
    private final Map<BaseSignature, MetadataMorphism> morphisms;

    MetadataCategory(SchemaCategory schema, Map<Key, MetadataObjex> objexes, Map<BaseSignature, MetadataMorphism> morphisms) {
        this.schema = schema;
        this.objexes = objexes;
        this.morphisms = morphisms;
    }

    public static MetadataCategory createEmpty(SchemaCategory schema) {
        return new MetadataCategory(schema, new TreeMap<>(), new TreeMap<>());
    }

    public @Nullable MetadataObjex getObjex(SchemaObjex objex) {
        return objexes.get(objex.key());
    }

    public @Nullable MetadataObjex getObjex(Key key) {
        return objexes.get(key);
    }

    public @Nullable MetadataMorphism getMorphism(SchemaMorphism morphism) {
        return morphisms.get(morphism.signature());
    }

    public @Nullable MetadataMorphism getMorphism(BaseSignature signature) {
        return morphisms.get(signature);
    }

    public void setObjex(SchemaObjex objex, @Nullable MetadataObjex metadataObjex) {
        setObjex(objex.key(), metadataObjex);
    }

    public void setObjex(Key key, @Nullable MetadataObjex metadataObjex) {
        if (metadataObjex == null)
            objexes.remove(key);
        else
            objexes.put(key, metadataObjex);
    }

    public void setMorphism(SchemaMorphism morphism, @Nullable MetadataMorphism metadataMorphism) {
        setMorphism(morphism.signature(), metadataMorphism);
    }

    public void setMorphism(BaseSignature signature, @Nullable MetadataMorphism metadataMorphism) {
        if (metadataMorphism == null)
            morphisms.remove(signature);
        else
            morphisms.put(signature, metadataMorphism);
    }

}
