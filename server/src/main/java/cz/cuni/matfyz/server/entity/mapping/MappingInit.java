package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    Id logicalModelId,
    SchemaObjectWrapper rootObject,
    Signature[] primaryKey,
    String kindName,
    ComplexProperty accessPath
) {
    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "logicalModelId");
    }
}
