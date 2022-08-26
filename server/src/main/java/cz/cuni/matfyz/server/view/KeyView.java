package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.schema.Key;

/**
 * @author jachym.bartik
 */
public record KeyView(
    int value
) {
    public KeyView(Key key) {
        this(key.getValue());
    }
}
