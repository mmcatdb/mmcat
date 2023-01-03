package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SignatureId;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record InstanceObjectWrapper(
    Key key,
    SignatureId superId,
    List<DomainRow> rows
) {
    public InstanceObjectWrapper(InstanceObject object) {
        this(
            object.key(),
            object.superId(),
            object.allRowsToSet().stream().toList()
        );
    }

}
