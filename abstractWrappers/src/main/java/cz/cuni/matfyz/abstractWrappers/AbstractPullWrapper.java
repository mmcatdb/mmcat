package cz.cuni.matfyz.abstractWrappers;

import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public interface AbstractPullWrapper {

	public abstract ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws Exception;

}
