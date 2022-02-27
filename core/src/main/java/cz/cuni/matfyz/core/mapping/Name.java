package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.utils.JSONConverterBase;
import cz.cuni.matfyz.core.utils.JSONConvertible;
import cz.cuni.matfyz.core.utils.JSONSwitchConverterBase;

import java.util.Set;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public abstract class Name implements JSONConvertible
{
    public static class Converter extends JSONSwitchConverterBase<Name> {

        @Override
        protected Set<JSONConverterBase<? extends Name>> getChildConverters() {
            return Set.of(
                new StaticName.Converter(),
                new DynamicName.Converter()
            );
        }
        
    }
}
