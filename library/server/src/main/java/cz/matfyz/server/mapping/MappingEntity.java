package cz.matfyz.server.mapping;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.utils.entity.VersionedEntity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class MappingEntity extends VersionedEntity {

    public final Id categoryId;
    public final Id datasourceId;
    public final Key rootObjexKey;
    public List<Signature> primaryKey;
    public String kindName;
    public ComplexProperty accessPath;

    private MappingEntity(
        Id id,
        Version version,
        Version lastValid,
        Id categoryId,
        Id datasourceId,
        Key rootObjexKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) {
        super(id, version, lastValid);

        this.categoryId = categoryId;
        this.datasourceId = datasourceId;
        this.rootObjexKey = rootObjexKey;
        this.primaryKey = primaryKey;
        this.kindName = kindName;
        this.accessPath = accessPath;
    }

    public static MappingEntity createNew(Version version, Id categoryId, Id datasourceId, Key rootObjexKey, List<Signature> primaryKey, String kindName, ComplexProperty accessPath) {
        return new MappingEntity(
            Id.createNew(),
            version,
            version,
            categoryId,
            datasourceId,
            rootObjexKey,
            primaryKey,
            kindName,
            accessPath
        );
    }

    public Mapping toMapping(Datasource datasource, SchemaCategory category) {
        return new Mapping(
            datasource,
            kindName,
            category,
            rootObjexKey,
            accessPath,
            primaryKey
        );
    }

    private record MappingJsonValue(
        Key rootObjexKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) implements Serializable {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(MappingJsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(MappingJsonValue.class);

    public static MappingEntity fromJsonValue(Id id, Version version, Version lastValid, Id categoryId, Id datasourceId, String jsonValue) throws JsonProcessingException {
        final MappingJsonValue json = jsonValueReader.readValue(jsonValue);
        return new MappingEntity(
            id,
            version,
            lastValid,
            categoryId,
            datasourceId,
            json.rootObjexKey(),
            json.primaryKey(),
            json.kindName(),
            json.accessPath()
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new MappingJsonValue(
            rootObjexKey,
            primaryKey,
            kindName,
            accessPath
        ));
    }

}
