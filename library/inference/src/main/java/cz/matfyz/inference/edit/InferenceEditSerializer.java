package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.io.Serializable;
import java.util.List;

// FIXME What is the purpose of this class? Isn't the `InferenceEdit` class already serializable? It implements `Serializable` and it uses `JsonSubTypes` to handle inheritance.
// Also, the `InferenceEdit` class is much more better - this one consists of mostly nullable fields, which are almost guaranteed to cause null pointer exceptions in the future.

/**
 * The {@code InferenceEditSerializer} class provides methods for serializing
 * and deserializing {@link InferenceEdit} objects. This is useful for converting
 * between different representations of edits for storage or transmission.
 */
public class InferenceEditSerializer {

    /**
     * A record class representing the serialized form of an {@link InferenceEdit}.
     */
    public record SerializedInferenceEdit(
        Integer id,
        boolean isActive,
        String type,
        Key primaryKey,
        Key primaryKeyIdentified,
        Key referenceKey,
        Key referredKey,
        List<Key> clusterKeys,
        List<PatternSegment> pattern,
        PrimaryKeyCandidate primaryKeyCandidate,
        ReferenceCandidate referenceCandidate
    ) implements Serializable {}

    /**
     * Serializes an {@link InferenceEdit} into a {@link SerializedInferenceEdit} record.
     *
     * @param inferenceEdit The {@link InferenceEdit} to serialize.
     * @return A {@link SerializedInferenceEdit} representing the serialized form of the input edit.
     */
    public static SerializedInferenceEdit serialize(InferenceEdit inferenceEdit) {
        String type = null;
        Key primaryKey = null;
        Key primaryKeyIdentified = null;
        Key referenceKey = null;
        Key referredKey = null;
        List<Key> clusterKeys = null;
        List<PatternSegment> pattern = null;
        PrimaryKeyCandidate primaryKeyCandidate = null;
        ReferenceCandidate referenceCandidate = null;

        if (inferenceEdit instanceof PrimaryKeyMerge.Data primaryKeyData) {
            type = "PrimaryKey";
            primaryKey = primaryKeyData.primaryKey;
            primaryKeyIdentified = primaryKeyData.primaryKeyIdentified;
            primaryKeyCandidate = primaryKeyData.candidate;
        } else if (inferenceEdit instanceof ReferenceMerge.Data referenceData) {
            type = "Reference";
            referenceKey = referenceData.referenceKey;
            referredKey = referenceData.referredKey;
            referenceCandidate = referenceData.candidate;
        } else if (inferenceEdit instanceof ClusterMerge.Data clusterData) {
            type = "Cluster";
            clusterKeys = clusterData.clusterKeys;
        } else if (inferenceEdit instanceof RecursionMerge.Data recursionData) {
            type = "Recursion";
            pattern = recursionData.pattern;
        }

        return new SerializedInferenceEdit(
            inferenceEdit.getId(),
            inferenceEdit.isActive(),
            type,
            primaryKey,
            primaryKeyIdentified,
            referenceKey,
            referredKey,
            clusterKeys,
            pattern,
            primaryKeyCandidate,
            referenceCandidate
        );
    }

    /**
     * Deserializes a {@link SerializedInferenceEdit} back into an {@link InferenceEdit} object.
     *
     * @param serializedInferenceEdit The {@link SerializedInferenceEdit} to deserialize.
     * @return The deserialized {@link InferenceEdit} object.
     */
    public static InferenceEdit deserialize(SerializedInferenceEdit serializedInferenceEdit) {
        String type = serializedInferenceEdit.type();
        InferenceEdit inferenceEdit = null;

        if ("PrimaryKey".equals(type)) {
            inferenceEdit = new PrimaryKeyMerge.Data(
                serializedInferenceEdit.id(),
                serializedInferenceEdit.isActive(),
                serializedInferenceEdit.primaryKey(),
                serializedInferenceEdit.primaryKeyIdentified(),
                serializedInferenceEdit.primaryKeyCandidate()
            );
        } else if ("Reference".equals(type)) {
            inferenceEdit = new ReferenceMerge.Data(
                serializedInferenceEdit.id(),
                serializedInferenceEdit.isActive(),
                serializedInferenceEdit.referenceKey(),
                serializedInferenceEdit.referredKey(),
                serializedInferenceEdit.referenceCandidate()
            );
        } else if ("Cluster".equals(type)) {
            inferenceEdit = new ClusterMerge.Data(
                serializedInferenceEdit.id(),
                serializedInferenceEdit.isActive(),
                serializedInferenceEdit.clusterKeys()
            );
        } else if ("Recursion".equals(type)) {
            inferenceEdit = new RecursionMerge.Data(
                serializedInferenceEdit.id(),
                serializedInferenceEdit.isActive(),
                serializedInferenceEdit.pattern()
            );
        }

        return inferenceEdit;
    }

}
