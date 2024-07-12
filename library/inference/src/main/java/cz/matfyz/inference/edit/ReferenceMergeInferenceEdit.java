package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = ReferenceMergeInferenceEdit.Deserializer.class)
public class ReferenceMergeInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "reference";

    public final Key referenceKey;
    public final Key referredKey;

    public ReferenceMergeInferenceEdit(Key referenceKey, Key referredKey) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
    }

    @Override
    public SchemaCategory applyEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: when there is a reference, the reference is an array object
         * and it has 2 outgoing morphism, one for _index and one for the original parent node
         */
        // TODO: the assumptions are not always true; review it and make it more general
        System.out.println("Applying Reference Merge Edit...");
        System.out.println("Reference Key: " + referenceKey);
        System.out.println("Referred Key: " + referredKey);

        SchemaObject dom = schemaCategory.getObject(referredKey);

        // add new morphisms
        Key referenceParentKey = getParentKey(schemaCategory, referenceKey);
        SchemaMorphism newMorphism = createNewMorphism(schemaCategory, dom, referenceParentKey);
        schemaCategory.addMorphism(newMorphism);

        Key indexKey = getIndexKey(schemaCategory, referenceKey);
        SchemaMorphism indexMorphism = createNewMorphism(schemaCategory, dom, indexKey);
        schemaCategory.addMorphism(indexMorphism);

        // remove the reference object and its morphisms
        schemaCategory = removeReferenceAndItsMorphisms(schemaCategory, Arrays.asList(referenceParentKey, indexKey));
        SchemaCategoryEditor editor = new SchemaCategoryEditor(schemaCategory);
        editor.deleteObject(referenceKey);

        return editor.schemaCategory;
    }

    private SchemaMorphism createNewMorphism(SchemaCategory schemaCategory, SchemaObject dom, Key codKey) {
        SchemaObject cod = schemaCategory.getObject(codKey);
        BaseSignature signature = Signature.createBase(getNewSignatureValue(schemaCategory));
        return new SchemaMorphism(signature, null, Min.ONE, new HashSet<>(), dom, cod);
    }

    private int getNewSignatureValue(SchemaCategory schemaCategory) {
        int max = 0;
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            // TODO: here I am relying on the fact, that in inference I create only BaseSignatures
            int signatureVal = Integer.parseInt(morphism.signature().toString());
            if (signatureVal > max) {
                max = signatureVal;
            }
        }
        return max + 1;
    }

    // TODO: make it more general
    private Key getParentKey(SchemaCategory schemaCategory, Key key) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(key) && !morphism.cod().label().equals("_index")) {
                return morphism.cod().key();
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    private Key getIndexKey(SchemaCategory schemaCategory, Key key) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(key) && morphism.cod().label().equals("_index")) {
                return morphism.cod().key();
            }
        }
        throw new NotFoundException("Index key has not been found");
    }

    private SchemaCategory removeReferenceAndItsMorphisms(SchemaCategory schemaCategory, List<Key> keysToDelete) {
        List<SchemaMorphism> morphismsToDelete = new ArrayList<>();
        for (SchemaMorphism morphismToDelete : schemaCategory.allMorphisms()) {
            if (morphismToDelete.dom().key().equals(referenceKey) && keysToDelete.contains(morphismToDelete.cod().key())) {
                morphismsToDelete.add(morphismToDelete);
            }
        }
        for (SchemaMorphism morphismToDelete : morphismsToDelete) {
            schemaCategory.removeMorphism(morphismToDelete);
        }
        return schemaCategory;
    }

    public static class Deserializer extends StdDeserializer<ReferenceMergeInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ReferenceMergeInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Key referenceKey = parser.getCodec().treeToValue(node.get("referenceKey"), Key.class);
            final Key referredKey = parser.getCodec().treeToValue(node.get("referredKey"), Key.class);

            return new ReferenceMergeInferenceEdit(referenceKey, referredKey);
        }
    }
}
