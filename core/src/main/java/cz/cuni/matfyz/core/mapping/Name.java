package cz.cuni.matfyz.core.mapping;

import java.util.Set;

import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.FromJSONSwitchBuilderBase;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public abstract class Name implements JSONConvertible
{
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
