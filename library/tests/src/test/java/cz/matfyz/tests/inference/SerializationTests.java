package cz.matfyz.tests.inference;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.InferenceEdit;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class SerializationTests {

    @Test
    void testInferenceEditSerialization() throws Exception {
        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(new Key(0));
        clusterKeys.add(new Key(1));

        final InferenceEdit edit = new ClusterMerge.Data(0, true, clusterKeys);

        ObjectMapper mapper = new ObjectMapper();

        String serializedEdit = mapper.writeValueAsString((InferenceEdit) edit);

        System.out.println(serializedEdit);

    }

}
