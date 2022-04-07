package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature.Type;

/**
 * 
 * @author jachym.bartik
 */
public class SignatureView {

    public int[] ids;
    public boolean isNull;

    public SignatureView(Signature signature) {
        this.ids = signature.ids();
        this.isNull = signature.getType() == Type.NULL;
    }

}
