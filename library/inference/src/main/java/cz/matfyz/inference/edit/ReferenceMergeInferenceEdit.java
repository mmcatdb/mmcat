package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
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
    ////////////////////////////
    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
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
        SchemaMorphism newMorphism = createMorphism(schemaCategory, dom, referenceParentKey);
        newReferenceSignature = newMorphism.signature();
        schemaCategory.addMorphism(newMorphism);

        Key indexKey = getIndexKey(schemaCategory, referenceKey);
        SchemaMorphism indexMorphism = createMorphism(schemaCategory, dom, indexKey);
        newIndexSignature = indexMorphism.signature();
        schemaCategory.addMorphism(indexMorphism);

        // remove the reference object and its morphisms
        schemaCategory = removeReferenceAndItsMorphisms(schemaCategory, Arrays.asList(referenceParentKey, indexKey));
        SchemaCategoryEditor editor = new SchemaCategoryEditor(schemaCategory);
        editor.deleteObject(referenceKey);

        return editor.schemaCategory;
    }

    private SchemaMorphism createMorphism(SchemaCategory schemaCategory, SchemaObject dom, Key codKey) {
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
        return schemaCategory;
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {

        // find the two mappings in question
        Mapping referenceMapping = findReferenceMapping(mappings);
        System.out.println("referenceMapping found: " + referenceMapping.accessPath());
        Mapping referredMapping = findReferredMapping(mappings);
        System.out.println("referredMapping found: " + referredMapping.accessPath());

        // create the new merged mapping
        ComplexProperty mergedComplexProperty = mergeComplexProperties(referenceMapping.accessPath(), referredMapping.accessPath());
        Mapping mergedMapping = new Mapping(schemaCategory, referenceKey, referenceMapping.kindName(), mergedComplexProperty, referenceMapping.primaryKey()); // TODO: what about the primary keys?
        System.out.println("mergedMapping: " + mergedMapping.accessPath());

        return updateMappings(mappings, referenceMapping, referredMapping, mergedMapping);
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            for (SchemaObject object : mapping.category().allObjects()) {
                if (object.key().equals(referenceKey)) {
                    return mapping;
                }
            }
        }
        throw new NotFoundException("Reference Mapping has not been found.");
    }

    private Mapping findReferredMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            for (SchemaObject object : mapping.category().allObjects()) {
                if (object.key().equals(referredKey)) {
                    return mapping;
                }
            }
        }
        throw new NotFoundException("Referred Mapping has not been found.");
    }

    // TODO: also set the new reference signature
    private ComplexProperty mergeComplexProperties(ComplexProperty referenceComplexProperty, ComplexProperty referredComplexProperty) {
        // find the subpath in the reference property with the target signature
        List<AccessPath> newSubpaths = new ArrayList<>();
        boolean replaced = false;

        for (AccessPath subpath : referenceComplexProperty.subpaths()) {
            if (!replaced && subpath.signature().equals(oldReferenceSignature)) {
                // replace it
                List<AccessPath> combinedSubpaths = new ArrayList<>(referredComplexProperty.subpaths());
                ComplexProperty newIndexComplexProperty = null;
                if (subpath instanceof ComplexProperty currentComplexProperty) {
                    AccessPath currentSubpath = currentComplexProperty.getSubpathBySignature(oldIndexSignature); // assuming there is just _index object
                    newIndexComplexProperty = new ComplexProperty(currentSubpath.name(), newIndexSignature, (List<AccessPath>) currentSubpath); //set the new index signature
                }
                combinedSubpaths.add(newIndexComplexProperty);
                newSubpaths.add(new ComplexProperty(subpath.name(), subpath.signature(), combinedSubpaths));
                replaced = true;
            } else {
                newSubpaths.add(subpath);
            }
        }
        return new ComplexProperty(referenceComplexProperty.name(), referenceComplexProperty.signature(), newSubpaths);
    }

    private List<Mapping> updateMappings(List<Mapping> mappings, Mapping referenceMapping, Mapping referredMapping, Mapping mergedMapping) {
        List<Mapping> updatedMappings = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (!mapping.equals(referenceMapping) && mapping.equals(referredMapping)) {
                updatedMappings.add(mapping);
            }
        }
        updatedMappings.add(mergedMapping);

        return updatedMappings;
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
