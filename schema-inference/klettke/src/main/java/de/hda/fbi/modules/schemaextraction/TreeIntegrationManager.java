package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;

import java.util.List;

/**
 * Integrates all necessary information into the schema trees
 *
 * @author daniel.mueller
 */
public interface TreeIntegrationManager {

	/**
	 * Integrates all necessary information into the schema trees
	 *
	 * @param extractionConfiguration
	 */
	void integrate(SchemaExtractionConfiguration extractionConfiguration);

	/**
	 * Integrates all necessary information into the schema trees
	 *
	 * @param entityinfos
	 */
	void integrate(List<EntityInfo> entityinfos);
}
