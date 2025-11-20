package cz.matfyz.server.exception;

import cz.matfyz.server.utils.entity.Id;

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

    public static NotFoundException primaryEntity(String type, Id id) {
        return new NotFoundException("primaryEntity", new Data(type, id));
    }

    public static NotFoundException secondaryEntity(String type, Id id) {
        return new NotFoundException("secondaryEntity", new Data(type, id));
    }

    public NotFoundException toSecondaryEntity() {
        final var data = (Data) this.data;
        return secondaryEntity(data.type, data.id);
    }

}
