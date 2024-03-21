package cz.matfyz.server.view;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceObject;

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
