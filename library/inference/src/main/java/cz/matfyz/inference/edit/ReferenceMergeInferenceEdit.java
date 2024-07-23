package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @JsonProperty("type")
    private final String type = "reference";

    public final Key referenceKey;
    public final Key referredKey;

    // these get initialized while running schema category edits
    private Signature oldReferenceSignature;
    private Signature newReferenceSignature;

    private Signature oldIndexSignature;
    private Signature newIndexSignature;

    public ReferenceMergeInferenceEdit(Key referenceKey, Key referredKey) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
    }

    //TODO: just for testing purposes, delete later
    ////////////////////////////////////////////
    public void setOldReferenceSig(Signature sig) {
        this.oldReferenceSignature = sig;
    }
    public void setNewReferenceSig(Signature sig) {
        this.newReferenceSignature = sig;
    }
    public void setOldIndexSig(Signature sig) {
        this.oldIndexSignature = sig;
    }
    public void setNewIndexSig(Signature sig) {
        this.newIndexSignature = sig;
    }
    public Signature getOldReferenceSig() {
        return this.oldReferenceSignature;
    }
    public Signature getNewReferenceSig() {
        return this.newReferenceSignature;
    }
    public Signature getOldIndexSig() {
        return this.oldIndexSignature;
    }
    public Signature getNewIndexSig() {
        return this.newIndexSignature;
    }
    ////////////////////////////
    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: when there is a reference, the reference is an array object
         * and it has 2 outgoing morphism, one for _index and one for the original parent node
         */
        // TODO: the assumptions are not always true; review it and make it more general
        System.out.println("Applying Reference Merge Edit on Schema Category...");
        System.out.println("Reference Key: " + referenceKey);
        System.out.println("Referred Key: " + referredKey);

        SchemaObject dom = schemaCategory.getObject(referredKey);

        // add new morphisms
        Key referenceParentKey = getParentKey(schemaCategory, referenceKey);
        newReferenceSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, referenceParentKey);

        Key indexKey = getIndexKey(schemaCategory, referenceKey);
        newIndexSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, indexKey);

        // remove the reference object and its morphisms
        removeReferenceMorphismsAndObject(schemaCategory, Arrays.asList(referenceParentKey, indexKey));

        return schemaCategory;
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

    private void removeReferenceMorphismsAndObject(SchemaCategory schemaCategory, List<Key> keysToDelete) {
        List<SchemaMorphism> morphismsToDelete = new ArrayList<>();
        for (SchemaMorphism morphismToDelete : schemaCategory.allMorphisms()) {
            if (morphismToDelete.dom().key().equals(referenceKey) && keysToDelete.contains(morphismToDelete.cod().key())) {
                morphismsToDelete.add(morphismToDelete);
                // find the reference and index signatures
                if (morphismToDelete.cod().label().equals("_index")) {
                    oldIndexSignature = morphismToDelete.signature();
                } else {
                    oldReferenceSignature = morphismToDelete.signature();
                }
            }
        }
        for (SchemaMorphism morphismToDelete : morphismsToDelete) {
            schemaCategory.removeMorphism(morphismToDelete);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schemaCategory);
        editor.deleteObject(referenceKey);
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        System.out.println("Applying Reference Merge Edit on Mapping...");

        // find the two mappings in question
        Mapping referenceMapping = findReferenceMapping(mappings);
        System.out.println("referenceMapping found: " + referenceMapping.accessPath());
        Mapping referredMapping = findReferredMapping(mappings, schemaCategory);
        System.out.println("referredMapping found: " + referredMapping.accessPath());

        // create the new merged mapping
        ComplexProperty mergedComplexProperty = mergeComplexProperties(referenceMapping.accessPath(), referredMapping.accessPath());
        // TODO; what about the primary keys here?
        Mapping mergedMapping = new Mapping(schemaCategory, referenceMapping.rootObject().key(), referenceMapping.kindName(), mergedComplexProperty, referenceMapping.primaryKey());
        System.out.println("mergedMapping: " + mergedMapping.accessPath());

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping, referredMapping), mergedMapping);
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(oldReferenceSignature.dual()) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping findReferredMapping(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // 1) in the schemaCategory find the signature where key is dom or cod
        // 2) check in which mapping this signature appears, it should appear in exactly one
        Signature referredSignature = null;
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(referredKey) || morphism.cod().key().equals(referredKey)) {
                if (!morphism.signature().equals(newIndexSignature) && !morphism.signature().equals(newReferenceSignature)) {
                    referredSignature = morphism.signature();
                }
            }
        }
        if (referredSignature == null) {
            throw new NotFoundException("Signature for referred object has not been found");
        }
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(referredSignature) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for referenc with signature " + referredSignature + " has not been found.");
    }

    // TODO: note all the places where we make the signature dual, this is because we assume, that the reference is a list
    private ComplexProperty mergeComplexProperties(ComplexProperty referenceComplexProperty, ComplexProperty referredComplexProperty) {
        // find the subpath in the reference property with the target signature
        List<AccessPath> newSubpaths = new ArrayList<>();
        boolean replaced = false;

        for (AccessPath subpath : referenceComplexProperty.subpaths()) {
            if (!replaced && subpath.signature().equals(oldReferenceSignature.dual())) {
                // replace it
                List<AccessPath> combinedSubpaths = new ArrayList<>(referredComplexProperty.subpaths());

                SimpleProperty newIndexSimpleProperty = null;

                if (subpath instanceof ComplexProperty currentComplexProperty) {
                    AccessPath currentSubpath = currentComplexProperty.getSubpathBySignature(oldIndexSignature); // assuming there is just _index object
                    System.out.println("currentSubpath: " + currentSubpath);

                    newIndexSimpleProperty = new SimpleProperty(currentSubpath.name(), newIndexSignature); //set the new index signature
                    System.out.println("newIndexCompelxProp: " + newIndexSimpleProperty);
                }
                combinedSubpaths.add(newIndexSimpleProperty);
                newSubpaths.add(new ComplexProperty(subpath.name(), newReferenceSignature.dual(), combinedSubpaths));
                replaced = true;
            } else {
                newSubpaths.add(subpath);
            }
        }
        return new ComplexProperty(referenceComplexProperty.name(), referenceComplexProperty.signature(), newSubpaths);
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
