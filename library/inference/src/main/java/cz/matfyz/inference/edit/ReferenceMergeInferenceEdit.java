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
        // Assumption: when there is a reference, it has exactly one incoming morphism
        System.out.println("SchemaCategory morphisms before reference merge edit: " + schemaCategory.allMorphisms());
        System.out.println("Applying Reference Merge Edit...");

        Key referenceParentKey = getParentKey(schemaCategory, referenceKey);
        SchemaObject dom = schemaCategory.getObject(referenceParentKey);
        SchemaObject cod = schemaCategory.getObject(referredKey);

        SchemaMorphism newMorphism = createNewMorphism(schemaCategory, dom, cod);

        System.out.println("new morphism: " + newMorphism);
        
        // add new morphism
        schemaCategory.addMorphism(newMorphism);

        // move the objects which made a morphism with the reference
        List<Key> referenceMorphismPairKeys = getMorphismPairKeys(schemaCategory, referenceKey);
        for (Key key : referenceMorphismPairKeys) {
            SchemaMorphism morphism = createNewMorphism(schemaCategory, dom, schemaCategory.getObject(key));
            schemaCategory.addMorphism(morphism);
        }

        // remove the reference object
        // TODO

        System.out.println("SchemaCategory morphisms after reference merge edit: " + schemaCategory.allMorphisms());

        return schemaCategory;
    }

    private SchemaMorphism createNewMorphism(SchemaCategory schemaCategory, SchemaObject dom, SchemaObject cod) {
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

    private Key getParentKey(SchemaCategory schemaCategory, Key key) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().key() == key) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    private List<Key> getMorphismPairKeys(SchemaCategory schemaCategory, Key key) {
        List<Key> morphismPairKeys = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key() == key) {
                morphismPairKeys.add(morphism.cod().key());
            }
        }
        return morphismPairKeys;
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
