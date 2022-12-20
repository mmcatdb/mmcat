package cz.cuni.matfyz.server.entity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class IdSerializer extends StdSerializer<Id> {
    
    public IdSerializer() {
        this(null);
    }
  
    public IdSerializer(Class<Id> t) {
        super(t);
    }

    @Override
    public void serialize(Id id, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(id.value);
    }

}
