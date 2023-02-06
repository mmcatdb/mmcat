package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SignatureId;
import cz.cuni.matfyz.server.utils.Position;

/**
 * @author jachym.bartik
 */
public record SchemaObjectWrapper(
    Key key,
    Position position,
    String label,
    SignatureId superId,
    ObjectIds ids,
    String iri,
    String pimIri
) {}
