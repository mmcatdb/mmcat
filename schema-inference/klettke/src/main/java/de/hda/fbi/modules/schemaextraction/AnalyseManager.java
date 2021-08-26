package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.common.AnalyseResult;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;

/**
 * This class is responsible for analysing entities of given extraction configuration
 *
 * @author daniel.mueller
 */
public interface AnalyseManager {

	/**
	 * It analyzes the entities and creates a SchemaExtractionConfiguration
	 *
	 * @param schemaExtractionConfiguration
	 * @return
	 * @throws de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException
	 */
	AnalyseResult analyse(SchemaExtractionConfiguration schemaExtractionConfiguration) throws SchemaExtractionException;
}
