package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = PrimaryKeyMergeInferenceEdit.Deserializer.class)
public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "primaryKey";

    public final Key primaryKeyRoot;
    public final Key primaryKey;

    public PrimaryKeyMergeInferenceEdit(Key primaryKeyRoot, Key primaryKey) {
        this.primaryKeyRoot = primaryKeyRoot;
        this.primaryKey = primaryKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: the primary key has a unique name. All the objects w/ this
         * name are the same primary key.
         */
        System.out.println("Applying Primary Key Merge Edit...");
        System.out.println("primary key root: " + primaryKeyRoot);
        System.out.println("primary key: " + primaryKey);

        SchemaObject dom = schemaCategory.getObject(primaryKeyRoot);

        String primaryKeyLabel = schemaCategory.getObject(primaryKey).label();
        System.out.println("primaryKeyLabel: " + primaryKeyLabel);

        // find all objects which I identified by the primary key
        List<Key> keysToProcess = findKeysIdentifiedByPrimaryKeyLabel(schemaCategory, primaryKeyLabel);
        System.out.println("keysToProcess: " + keysToProcess);

        // create morphisms from these keys to the primary root key
        for (Key keyToProcess : keysToProcess) {
            // this method returns signature, which I might need later
            InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, keyToProcess);
        }

        // delete the old morphisms to the primary key and old primary key objects
        schemaCategory = removePrimaryKeyMorphismsAndObjects(schemaCategory, keysToProcess, primaryKeyLabel);

        return schemaCategory;
    }

    private List<Key> findKeysIdentifiedByPrimaryKeyLabel(SchemaCategory schemaCategory, String primaryKeyLabel) {
        List<Key> keysIdentifiedByPrimaryKey = new ArrayList<>();

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && !morphism.dom().key().equals(primaryKeyRoot)) {
                keysIdentifiedByPrimaryKey.add(morphism.dom().key());
            }
        }
        return keysIdentifiedByPrimaryKey;
    }

    private SchemaCategory removePrimaryKeyMorphismsAndObjects(SchemaCategory schemaCategory, List<Key> keysToProcess, String primaryKeyLabel) {
        List<SchemaMorphism> morphismsToDelete = new ArrayList<>();
        List<Key> keysToDelete = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && keysToProcess.contains(morphism.dom().key())) {
                morphismsToDelete.add(morphism);
                keysToDelete.add(morphism.cod().key());
            }
        }
        System.out.println("morphisms to delete: " + morphismsToDelete);
        System.out.println("keys to delete: " + keysToDelete);
        for (SchemaMorphism morphismToDelete : morphismsToDelete) {
            schemaCategory.removeMorphism(morphismToDelete);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schemaCategory);
        editor.deleteObjects(keysToDelete);
        return schemaCategory;
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applyMappingEdit'");
    }

    public static class Deserializer extends StdDeserializer<PrimaryKeyMergeInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public PrimaryKeyMergeInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Key primaryKeyRoot = parser.getCodec().treeToValue(node.get("primaryKeyRoot"), Key.class);
            final Key primaryKey = parser.getCodec().treeToValue(node.get("primaryKey"), Key.class);

            return new PrimaryKeyMergeInferenceEdit(primaryKeyRoot, primaryKey);
        }
    }

}
