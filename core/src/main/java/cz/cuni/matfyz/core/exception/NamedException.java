package cz.cuni.matfyz.core.exception;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A base class for all exceptions. It is supposed to be recognized by the client application.
 * @author jachymb.bartik
 */
@JsonSerialize(using = NamedException.Serializer.class)
public abstract class NamedException extends RuntimeException {
    
    private final String name;
    protected final Serializable data;

    protected NamedException(String name, Serializable data, Throwable cause) {
        super(createMessage(name, data), cause);
        this.name = name;
        this.data = data;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static String createMessage(String name, Serializable data) {
        if (data == null)
            return name;

        String dataString = "";
        try {
            dataString = mapper.writeValueAsString(data);
        }
        catch (Exception e) {
            dataString = "invalid data";
        }

        return name + " (" + dataString + ")";
    }

    public record SerializedException(
        String name,
        Serializable data
    ) implements Serializable {}

    public SerializedException toSerializedException() {
        return new SerializedException(name, data);
    }

    public static class Serializer extends StdSerializer<NamedException> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<NamedException> t) {
            super(t);
        }

        @Override
        public void serialize(NamedException exception, JsonGenerator generator, SerializerProvider provider) throws IOException {
            final var serializedException = exception.toSerializedException();
            generator.writeObject(serializedException);
        }

    }

}
