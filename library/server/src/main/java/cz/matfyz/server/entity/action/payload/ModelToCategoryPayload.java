package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.ActionPayload;

/**
 * @author jachym.bartik
 */
public record ModelToCategoryPayload(
    Id logicalModelId
) implements ActionPayload {}
