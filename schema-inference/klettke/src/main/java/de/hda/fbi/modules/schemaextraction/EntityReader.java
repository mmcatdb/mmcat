package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;

import java.util.List;

/**
 * In this class, the entities are imported from the database into application
 *
 * @author daniel.mueller
 */
public interface EntityReader {

	/**
	 * Reads the entities from the database and stores them in the EntityInfo object structure
	 *
	 * @param extractionConfiguration
	 * @param entityName
	 * @return
	 */
	List<EntityInfo> readEntitiesSorted(SchemaExtractionConfiguration extractionConfiguration, List<String> entityName);
}
