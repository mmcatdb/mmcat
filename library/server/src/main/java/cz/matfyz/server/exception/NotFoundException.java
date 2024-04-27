package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends ServerException {

    private NotFoundException(String name, Data data) {
        super("notFound." + name, data, null);
    }

    private record Data(
        String type,
        Id id
    ) implements Serializable{}

    public static NotFoundException primaryObject(String type, Id id) {
        return new NotFoundException("primaryObject", new Data(type, id));
    }

    public static NotFoundException secondaryObject(String type, Id id) {
        return new NotFoundException("secondaryObject", new Data(type, id));
    }

    public NotFoundException toSecondaryObject() {
        final var data = (Data) this.data;
        return secondaryObject(data.type, data.id);
    }

}
