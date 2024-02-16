package cz.matfyz.core.querying.queryresult;

import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface ResultNode extends Printable {

    public static interface NodeBuilder {

        ResultNode build();

    }

    public static class JsonBuilder {

        private static ObjectMapper mapper = new ObjectMapper();

        public static ResultNode fromJson(String json) throws JsonProcessingException {
            final var node = mapper.readTree(json);

            return processNode(node);
        }

        private static ResultNode processNode(JsonNode node) {
            if (node.isArray())
                return processArray(node);
            if (node.isObject())
                return processObject(node);

            return new ResultLeaf(node.asText());
        }

        private static ResultList processArray(JsonNode array) {
            final var list = new ArrayList<ResultNode>();
            array.elements().forEachRemaining(node -> list.add(processNode(node)));

            return new ResultList(list);
        }

        private static ResultMap processObject(JsonNode object) {
            final var builder = new ResultMap.Builder();
            object.fieldNames().forEachRemaining(name -> builder.put(name, processNode(object.get(name))));

            return builder.build();
        }

    }

}