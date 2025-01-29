package cz.matfyz.core.querying;

import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface ResultNode extends Printable {

    interface NodeBuilder {

        ResultNode build();

    }

    class JsonBuilder {

        private JsonBuilder() {}

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

            return new LeafResult(node.asText());
        }

        private static ListResult processArray(JsonNode array) {
            final var list = new ArrayList<ResultNode>();
            array.elements().forEachRemaining(node -> list.add(processNode(node)));

            return new ListResult(list);
        }

        private static MapResult processObject(JsonNode object) {
            final var builder = new MapResult.Builder();
            object.fieldNames().forEachRemaining(name -> builder.put(name, processNode(object.get(name))));

            return builder.build();
        }

    }

}
