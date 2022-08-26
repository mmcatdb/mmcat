package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.FromJSONSwitchBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import java.util.Set;

/**
 * @author pavel.koupil, jachym.bartik
 */
public abstract class Name implements JSONConvertible {

    protected Name() {}

    public static class Builder extends FromJSONSwitchBuilderBase<Name> {

        @Override
        protected Set<FromJSONBuilderBase<? extends Name>> getChildConverters() {
            return Set.of(
                new StaticName.Builder(),
                new DynamicName.Builder()
            );
        }
        
    }
}
