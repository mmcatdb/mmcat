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

public class InferenceEditSerializer {

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
            primaryKey = primaryKeyData.getPrimaryKey();
            primaryKeyIdentified = primaryKeyData.getPrimaryKeyIdentified();
            primaryKeyCandidate = primaryKeyData.getCandidate();
        } else if (inferenceEdit instanceof ReferenceMerge.Data referenceData) {
            type = "Reference";
            referenceKey = referenceData.getReferenceKey();
            referredKey = referenceData.getReferredKey();
            referenceCandidate = referenceData.getCandidate();
        } else if (inferenceEdit instanceof ClusterMerge.Data clusterData) {
            type = "Cluster";
            clusterKeys = clusterData.getClusterKeys();
        } else if (inferenceEdit instanceof RecursionMerge.Data recursionData) {
            type = "Recursion";
            pattern = recursionData.getPattern();
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
