package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.VersionedEntity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class MappingWrapper extends VersionedEntity {

    public final Id categoryId;
    public final Id datasourceId;
    public final Key rootObjectKey;
    public final List<Signature> primaryKey;
    public final String kindName;
    public final ComplexProperty accessPath;

    private MappingWrapper(
        Id id,
        Version version,
        Version lastValid,
        Id categoryId,
        Id datasourceId,
        Key rootObjectKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) {
        super(id, version, lastValid);

        this.categoryId = categoryId;
        this.datasourceId = datasourceId;
        this.rootObjectKey = rootObjectKey;
        this.primaryKey = primaryKey;
        this.kindName = kindName;
        this.accessPath = accessPath;
    }

    public static MappingWrapper createNew(Version version, Id categoryId, Id datasourceId, Key rootObjectKey, List<Signature> primaryKey, String kindName, ComplexProperty accessPath) {
        return new MappingWrapper(
            Id.createNew(),
            version,
            version,
            categoryId,
            datasourceId,
            rootObjectKey,
            primaryKey,
            kindName,
            accessPath
        );
    }

    public Mapping toMapping(SchemaCategory category) {
        return new Mapping(
            category,
            rootObjectKey,
            kindName,
            accessPath,
            primaryKey
        );
    }

    private record MappingJsonValue(
        Key rootObjectKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) implements Serializable {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(MappingJsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(MappingJsonValue.class);

    public static MappingWrapper fromJsonValue(Id id, Version version, Version lastValid, Id categoryId, Id datasourceId, String jsonValue) throws JsonProcessingException {
        final MappingJsonValue json = jsonValueReader.readValue(jsonValue);
        return new MappingWrapper(
            id,
            version,
            lastValid,
            categoryId,
            datasourceId,
            json.rootObjectKey(),
            json.primaryKey(),
            json.kindName(),
            json.accessPath()
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new MappingJsonValue(
            rootObjectKey,
            primaryKey,
            kindName,
            accessPath
        ));
    }

}
