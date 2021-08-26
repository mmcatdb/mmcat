package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;

/**
 * This class is intended to create the decision table
 *
 * @author daniel.mueller
 */
public interface DecisionTableCreator {

	/**
	 * Creates the decision table by analysing the entities by given schema extraction configuration
	 *
	 * @param extractionConfiguration
	 * @return
	 */
	DecisionTable createDecisionTable(SchemaExtractionConfiguration extractionConfiguration);

}
