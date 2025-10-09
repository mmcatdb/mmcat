package cz.matfyz.wrapperjson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Parses an input stream to a stream of JSON objects.
 * Also supports the JSON Lines format (newline-delimited JSON).
 */
class JsonParsedIterator implements Iterator<ObjectNode> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonParser parser;

    private ObjectNode next;

    private JsonParsedIterator(JsonParser parser) {
        this.parser = parser;
    }

    static Stream<ObjectNode> toStream(InputStream input) throws IOException {
        final var parser = new JsonFactory().createParser(input);
        final var iterator = new JsonParsedIterator(parser);
        iterator.advance();

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
            .onClose(() -> {
                try {
                    parser.close();
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    @Override public boolean hasNext() {
        return next != null;
    }

    @Override public ObjectNode next() {
        if (next == null)
            throw new NoSuchElementException();

        try {
            final var output = next;
            advance();
            return output;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void advance() throws IOException {
        next = null;

        while (parser.nextToken() != null) {
            // This is kinda hacky. We basically skip everything until the start of the next object. Then we read the object and continue.
            // This way we support both arrays of objects and JSON Lines. However, we don't support several nested top-level arrays.
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                next = objectMapper.readTree(parser);
                return;
            }
        }
    }

}
