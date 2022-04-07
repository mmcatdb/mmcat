package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.schema.Key;

/**
 * 
 * @author jachym.bartik
 */
public class KeyView {

    public int value;

    public KeyView(Key key) {
        this.value = key.getValue();
    }

}
