package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;
import cz.matfyz.inference.edit.utils.PatternSegment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = RecursionInferenceEdit.Deserializer.class)
public class RecursionInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "recursion";

    public final List<PatternSegment> pattern;

    private Set<Signature> signaturesToDelete = new HashSet<>();
    private Set<Key> keysToDelete = new HashSet<>();

    public RecursionInferenceEdit(List<PatternSegment> pattern) {
        this.pattern = pattern;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {      
        // find the pattern occurence
        List<List<SchemaObject>> occurences = findPatternOccurences(schemaCategory);
        System.out.println("occurences: " + occurences);

        System.out.println("SK objects before replacement: " + schemaCategory.allObjects());
        System.out.println("morphisms before change");
        for (SchemaMorphism m: schemaCategory.allMorphisms()) {
            System.out.println("dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

        Map<List<SchemaObject>, Key> mapOccurenceNewKey = new HashMap<>();
        String patternName = createPatternName();
        for (List<SchemaObject> occurrence : occurences) {
            createNewRecursionObject(schemaCategory, occurrence, mapOccurenceNewKey, patternName);
        }
        for (List<SchemaObject> occurrence : occurences) {
            createNewRecursionMorphisms(schemaCategory, occurrence, mapOccurenceNewKey);
        }

        System.out.println("SK objects after replacement: " + schemaCategory.allObjects());

        System.out.println("signatures to delete: " + signaturesToDelete);
        System.out.println("keys to delete: " + keysToDelete);

        InferenceEditorUtils.removeMorphismsAndObjects(schemaCategory, List.copyOf(signaturesToDelete), List.copyOf(keysToDelete));

        System.out.println("final morphisms:");
        for (SchemaMorphism m : schemaCategory.allMorphisms()) {
            System.out.println("dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
            System.out.println();
        }

        return schemaCategory;
    }

    private List<List<SchemaObject>> findPatternOccurences(SchemaCategory schemaCategory) {
        List<List<SchemaObject>> result = new ArrayList<>();
        for (SchemaObject node : schemaCategory.allObjects()) {
            dfsFind(schemaCategory, node, 0, new ArrayList<>(), result);
        }
        return result;
    }

    private void dfsFind(SchemaCategory schemaCategory, SchemaObject currentNode, int patternIndex, List<SchemaObject> currentPath, List<List<SchemaObject>> result) {
        if (currentNode == null || patternIndex >= pattern.size()) return;

        PatternSegment currentSegment = pattern.get(patternIndex);
        if (currentNode.label().equals(currentSegment.nodeName)) {
            currentPath.add(currentNode);

            if (patternIndex == pattern.size() - 1) {
                // found full match
                result.add(new ArrayList<>(currentPath));
            } else {
                if (currentSegment.direction.equals("->")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.dom().equals(currentNode)) {
                            dfsFind(schemaCategory, morphism.cod(), patternIndex + 1, currentPath, result);
                        }
                    }
                } else if (currentSegment.direction.equals("<-")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.cod().equals(currentNode)) {
                            dfsFind(schemaCategory, morphism.dom(), patternIndex + 1, currentPath, result);
                        }
                    }
                }
            }
            currentPath.remove(currentPath.size() - 1);
        }
    }

    private String createPatternName() {
        StringBuilder sb = new StringBuilder();
        for (PatternSegment patternSegment : pattern) {
            sb.append(patternSegment);
        }
        return sb.toString().trim();
    }

    private void createNewRecursionObject(SchemaCategory schemaCategory, List<SchemaObject> occurrence, Map<List<SchemaObject>, Key> mapOccurenceNewKey, String patternName) {
        Key newKey = InferenceEditorUtils.createAndAddObject(schemaCategory, patternName, ObjectIds.createGenerated());
        mapOccurenceNewKey.put(occurrence, newKey);
        for (SchemaObject so : occurrence) {
            keysToDelete.add(so.key());
        }
    }

    private void createNewRecursionMorphisms(SchemaCategory schemaCategory, List<SchemaObject> occurrence, Map<List<SchemaObject>, Key> mapOccurenceNewKey) {
        SchemaObject first = occurrence.get(0);
        SchemaObject last = occurrence.get(occurrence.size() - 1);

        Key newKey = mapOccurenceNewKey.get(occurrence);

        List<SchemaObject> domObjects = getUpdatedDomObjects(schemaCategory, mapOccurenceNewKey, first, newKey);
        List<SchemaObject> codObjects = getUpdatedCodObjects(schemaCategory, mapOccurenceNewKey, last, newKey);

        for (SchemaObject dom : domObjects) {
            InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, newKey);
        }
        for (SchemaObject cod : codObjects) {
            InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(newKey), cod.key());
        }

        findPatternSegmentSignaturesToDelete(schemaCategory, occurrence);
    }

    private List<SchemaObject> getUpdatedDomObjects(SchemaCategory schemaCategory, Map<List<SchemaObject>, Key> mapOccurenceNewKey, SchemaObject first, Key newKey) {
        List<SchemaObject> domObjects = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().equals(first)) {
                if (keysToDelete.contains(morphism.dom().key())) {
                    domObjects.add(schemaCategory.getObject(findNewKeyInstead(mapOccurenceNewKey, morphism.dom().key(), newKey)));
                } else {
                    domObjects.add(morphism.dom());
                }
                signaturesToDelete.add(morphism.signature());
            }
        }
        return domObjects;
    }

    private List<SchemaObject> getUpdatedCodObjects(SchemaCategory schemaCategory, Map<List<SchemaObject>, Key> mapOccurenceNewKey, SchemaObject last, Key newKey) {
        List<SchemaObject> codObjects = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().equals(last)) {
                if (keysToDelete.contains(morphism.cod().key())) {
                    codObjects.add(schemaCategory.getObject(findNewKeyInstead(mapOccurenceNewKey, morphism.cod().key(), newKey)));
                } else {
                    codObjects.add(morphism.cod());
                }
                signaturesToDelete.add(morphism.signature());
            }
        }
        return codObjects;
    }

    public Key findNewKeyInstead(Map<List<SchemaObject>, Key> mapOccurenceNewKey, Key keyInOccurence, Key newKey) {
        for (List<SchemaObject> occurence : mapOccurenceNewKey.keySet()) {
            if (!mapOccurenceNewKey.get(occurence).equals(newKey)) {
                for (SchemaObject schemaObject : occurence) {
                    if (keyInOccurence.equals(schemaObject.key())) {
                        return mapOccurenceNewKey.get(occurence);
                    }
                }
                
            }
        }
        throw new NotFoundException("New Key for key " + keyInOccurence + " has not been found");
    }

    private void findPatternSegmentSignaturesToDelete(SchemaCategory schemaCategory, List<SchemaObject> occurrence) {
        for (int i = 0; i < occurrence.size() - 1; i++) {
            PatternSegment patternSegment = pattern.get(i);
            boolean aIsDom = patternSegment.direction.equals("->");
            signaturesToDelete.add(findSignatureFromObjects(schemaCategory, occurrence.get(i), occurrence.get(i + 1), aIsDom));
        }
    }

    public Signature findSignatureFromObjects(SchemaCategory schemaCategory, SchemaObject a, SchemaObject b, boolean aIsDom) {
        SchemaObject dom = aIsDom ? a : b;
        SchemaObject cod = aIsDom ? b : a;

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod)) {
                return morphism.signature();
            }
        }
        throw new NotFoundException("SchemaMorphism between " + a + " and " + b + " has not been found");
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applyMappingEdit'");
    }

    public static class Deserializer extends StdDeserializer<RecursionInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public RecursionInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final List<PatternSegment> pattern = new ArrayList<>();
            final JsonNode patternNode = node.get("pattern");

            if (pattern != null) {
                for (JsonNode patternSegmentNode : patternNode) {
                    PatternSegment patternSegment = parser.getCodec().treeToValue(patternSegmentNode, PatternSegment.class);
                    pattern.add(patternSegment);
                }
            }
            return new RecursionInferenceEdit(pattern);
        }
    }
}
