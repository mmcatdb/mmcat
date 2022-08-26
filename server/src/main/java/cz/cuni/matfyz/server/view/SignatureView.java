package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature.Type;

/**
 * @author jachym.bartik
 */
public class SignatureView {

    public final int[] ids;
    public final boolean isNull;

    public SignatureView(Signature signature) {
        this.ids = signature.ids();
        this.isNull = signature.getType() == Type.NULL;
    }

}
