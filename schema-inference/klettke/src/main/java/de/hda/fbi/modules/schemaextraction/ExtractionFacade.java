package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.common.ExtractionResult;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;

/**
 * Access point for the schema extraction
 *
 * @author daniel.mueller
 */
public interface ExtractionFacade {

	/**
	 * Starts the schema extraction algorithm by given configuration
	 *
	 * @param extractionConfiguration
	 * @return
	 * @throws de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException
	 */
	ExtractionResult start(SchemaExtractionConfiguration extractionConfiguration) throws SchemaExtractionException;

	/**
	 * Updates the decision table and clarifies the decision table by given decision
	 *
	 * @param decisionTable
	 * @param decisionTableEntryId
	 * @param decisionCommandIndex
	 * @param sourceProperty
	 * @param targetProperty
	 * @return
	 */
	ExtractionResult clarifiyDecision(DecisionTable decisionTable, int decisionTableEntryId,
			Integer decisionCommandIndex, String sourceProperty, String targetProperty);
}
