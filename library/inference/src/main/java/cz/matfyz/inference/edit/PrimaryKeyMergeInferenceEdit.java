package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = PrimaryKeyMergeInferenceEdit.Deserializer.class)
public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMergeInferenceEdit.class.getName());

    @JsonProperty("type")
    private final String type = "primaryKey";

    public final Key primaryKey;

    private Key primaryKeyRoot;
    private List<Key> keysIdentifiedByPrimary;
    private Map<Key, Signature> newSignatureMap;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    public PrimaryKeyMergeInferenceEdit(Key primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: the primary key has a unique name. All the objects w/ this
         * name are the same primary keys. The primary key is a single object
         */
        LOGGER.info("Applying Primary Key Merge Edit on Schema Category...");

        setSchemaCategories(schemaCategory);

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchemaCategory);
        SchemaObject dom = newSchemaCategory.getObject(primaryKeyRoot);

        String primaryKeyLabel = newSchemaCategory.getObject(primaryKey).label();
        this.keysIdentifiedByPrimary = findKeysIdentifiedByPrimaryKeyLabel(newSchemaCategory, primaryKeyLabel);

        this.newSignatureMap = createNewMorphisms(newSchemaCategory, dom);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        return newSchemaCategory;
    }

    private Key findPrimaryKeyRoot(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            // based on the assumption
            if (morphism.cod().key().equals(primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Root has not been found");
    }

    private List<Key> findKeysIdentifiedByPrimaryKeyLabel(SchemaCategory schemaCategory, String primaryKeyLabel) {
        List<Key> keys = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && !morphism.dom().key().equals(primaryKeyRoot)) {
                keys.add(morphism.dom().key());

                signaturesToDelete.add(morphism.signature());
                keysToDelete.add(morphism.cod().key());
                oldSignatureMap.put(morphism.dom().key(), morphism.signature());
            }
        }
        return keys;
    }

    private Map<Key, Signature> createNewMorphisms(SchemaCategory schemaCategory, SchemaObject dom) {
        Map<Key, Signature> signatureMap = new HashMap<>();
        for (Key key : keysIdentifiedByPrimary) {
            Signature newSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, schemaCategory.getObject(key));
            signatureMap.put(key, newSignature);
        }
        return signatureMap;
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        /*
         * Assumption: When we find object which is identified by the primary key,
         * we assume that the object is a root in its "part" of the schemaCategory
         */
        LOGGER.info("Applying Primary Key Merge Edit on Mapping...");

        List<Mapping> primaryKeyMappings = findMappingsWithPrimaryKey(mappings);

        List<Mapping> cleanedPrimaryKeyMappings = cleanPrimaryKeyMappings(primaryKeyMappings);

        return InferenceEditorUtils.updateMappings(mappings, primaryKeyMappings, cleanedPrimaryKeyMappings);
    }

    private List<Mapping> findMappingsWithPrimaryKey(List<Mapping> mappings) {
        List<Mapping> primaryKeyMappings = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (keysIdentifiedByPrimary.contains(mapping.rootObject().key())) {
                primaryKeyMappings.add(mapping);
            }
        }
        return primaryKeyMappings;
    }

    private List<Mapping> cleanPrimaryKeyMappings(List<Mapping> primaryKeyMappings) {
        List<Mapping> cleanedPrimaryKeyMappings = new ArrayList<>();
        for (Mapping mapping : primaryKeyMappings) {
            cleanedPrimaryKeyMappings.add(createCleanedMapping(mapping));
        }
        return cleanedPrimaryKeyMappings;
    }

    private Mapping createCleanedMapping(Mapping mapping) {
        ComplexProperty cleanedComplexProperty = cleanComplexProperty(mapping);
        return new Mapping(newSchemaCategory, mapping.rootObject().key(), mapping.kindName(), cleanedComplexProperty, mapping.primaryKey());
    }

    private ComplexProperty cleanComplexProperty(Mapping mapping) {
        ComplexProperty complexProperty = mapping.accessPath();
        Signature oldSignature = oldSignatureMap.get(mapping.rootObject().key());
        AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(oldSignature);
        return complexProperty.minusSubpath(accessPathToDelete);
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

            final Key primaryKey = parser.getCodec().treeToValue(node.get("primaryKey"), Key.class);

            PrimaryKeyMergeInferenceEdit edit = new PrimaryKeyMergeInferenceEdit(primaryKey);

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
