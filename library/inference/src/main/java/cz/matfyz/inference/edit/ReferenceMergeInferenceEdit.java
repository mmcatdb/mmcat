package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = ReferenceMergeInferenceEdit.Deserializer.class)
public class ReferenceMergeInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(ReferenceMergeInferenceEdit.class.getName());
    private static final String INDEX_LABEL = "_index";

    @JsonProperty("type")
    private final String type = "reference";

    public final Key referenceKey;
    public final Key referredKey;

    private boolean referenceIsArray;

    private Signature oldReferenceSignature;
    private Signature newReferenceSignature;
    private Signature oldIndexSignature;
    private Signature newIndexSignature;

    public ReferenceMergeInferenceEdit(Key referenceKey, Key referredKey) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: when there is a reference and it is an array object
         * it has 2 outgoing morphism, one for _index and one for the original parent node
         * If it is not array, it has only one ingoing morphism from the root
         */
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

        setSchemaCategories(schemaCategory);

        this.referenceIsArray = isReferenceArray(newSchemaCategory);

        SchemaObject referredObject = newSchemaCategory.getObject(referredKey);

        Key referenceParentKey = getReferenceParentKey(newSchemaCategory, referenceIsArray);

        SchemaObject dom = newSchemaCategory.getObject(referenceParentKey);
        SchemaObject cod = referredObject;
        Key indexKey = null;
        if (referenceIsArray) {
            dom = referredObject;
            cod = newSchemaCategory.getObject(referenceParentKey);

            indexKey = getIndexKey(newSchemaCategory, referenceKey);
            this.newIndexSignature = InferenceEditorUtils.createAndAddMorphism(newSchemaCategory, referredObject, newSchemaCategory.getObject(indexKey));
        }
        this.newReferenceSignature = InferenceEditorUtils.createAndAddMorphism(newSchemaCategory, dom, cod);

        findMorphismsAndObjectToDelete(newSchemaCategory, indexKey);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        return newSchemaCategory;
    }

    private boolean isReferenceArray(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(referenceKey) && morphism.cod().label().equals(INDEX_LABEL)) {
                return true;
            }
        }
        return false;
    }

    // based on the assumptions
    private Key getReferenceParentKey(SchemaCategory schemaCategory, boolean isReferenceArray) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (isReferenceArray) {
                if (morphism.dom().key().equals(referenceKey) && !morphism.cod().label().equals(INDEX_LABEL)) {
                    return morphism.cod().key();
                }
            } else {
                if (morphism.cod().key().equals(referenceKey)) {
                    return morphism.dom().key();
                }
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    private Key getIndexKey(SchemaCategory schemaCategory, Key key) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(key) && morphism.cod().label().equals(INDEX_LABEL)) {
                return morphism.cod().key();
            }
        }
        throw new NotFoundException("Index key has not been found");
    }

    private void findMorphismsAndObjectToDelete(SchemaCategory schemaCategory, Key indexKey) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (indexKey != null && morphism.dom().key().equals(referenceKey)) {
                signaturesToDelete.add(morphism.signature());
                // find the reference and index signatures
                if (morphism.cod().key().equals(indexKey)) {
                    oldIndexSignature = morphism.signature();
                } else {
                    oldReferenceSignature = morphism.signature();
                }
            } else {
                if (morphism.cod().key().equals(referenceKey)) {
                    signaturesToDelete.add(morphism.signature());
                    oldReferenceSignature = morphism.signature();
                }
            }
        }
        keysToDelete.add(referenceKey);
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        /*
         * No edit required
         */
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        /*
        if (referenceIsArray) {
            this.oldReferenceSignature = this.oldReferenceSignature.dual();
            this.newReferenceSignature = this.newReferenceSignature.dual();
        }*/

        // Mapping referenceMapping = findReferenceMapping(mappings);
        // Mapping referredMapping = findReferredMapping(mappings, newSchemaCategory);

        return mappings;
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(oldReferenceSignature) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping findReferredMapping(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // 1) in the schemaCategory find the signature where key is dom or cod
        // 2) check in which mapping this signature appears, it should appear in exactly one
        Signature referredSignature = findReferredSignature(schemaCategory);
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(referredSignature) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for referred with signature " + referredSignature + " has not been found.");
    }

    private Signature findReferredSignature(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if ((morphism.dom().key().equals(referredKey) || morphism.cod().key().equals(referredKey)) && !morphism.signature().equals(newIndexSignature)) {
                if (referenceIsArray) {
                    if (!morphism.signature().equals(newReferenceSignature.dual())) {
                        return morphism.signature();
                    }
                } else {
                    if (!morphism.signature().equals(newReferenceSignature)) {
                        return morphism.signature();
                    }
                }
            }
        }
        throw new NotFoundException("Signature for referred object has not been found");
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

            ReferenceMergeInferenceEdit edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey);

            if (node.has("isActive")) {
                edit.setActive(node.get("isActive").asBoolean());
            }

            if (node.has("id") && node.get("id") != null) {
                edit.setId(node.get("id").asInt());
            }

            return edit;
        }
    }
}
