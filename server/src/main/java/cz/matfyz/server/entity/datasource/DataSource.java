package cz.matfyz.server.entity.datasource;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author jachym.bartik
 */
@JsonDeserialize(using = DataSource.Deserializer.class)
public class DataSource extends Entity {

    public String url;
    public String label;
    public Type type;

    private DataSource(Id id) {
        super(id);
    }

    public enum Type {
        JsonLdStore
    }

    public void updateFrom(DataSourceUpdate update) {
        if (update.url() != null)
            this.url = update.url();
        
        if (update.label() != null)
            this.label = update.label();
    }

    public static class Builder {

        private static final ObjectReader dataSourceJsonReader = new ObjectMapper().readerFor(DataSource.class);

        public DataSource fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
            return dataSourceJsonReader
                .withAttribute("id", id)
                .readValue(jsonValue);
        }

        public DataSource fromDataSource(Id id, DataSource input) {
            final var dataSource = new DataSource(id);
            dataSource.url = input.url;
            dataSource.label = input.label;
            dataSource.type = input.type;

            return dataSource;
        }

        public DataSource fromInit(DataSourceInit init) {
            final var dataSource = new DataSource(null);
            dataSource.url = init.url();
            dataSource.label = init.label();
            dataSource.type = init.type();

            return dataSource;
        }

    }

    public static class Deserializer extends StdDeserializer<DataSource> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public DataSource deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final var id = (Id) context.getAttribute("id");

            final var dataSource = new DataSource(id);

            dataSource.url = node.get("url").asText();
            dataSource.label = node.get("label").asText();
            dataSource.type = Type.valueOf(node.get("type").asText());

            return dataSource;
        }

    }
    
}
