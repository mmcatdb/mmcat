package de.hda.fbi.modules.schemaextraction;

import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;

/**
 * This class updates the decision table
 *
 * @author daniel.mueller
 */
public interface DecisionManager {

	/**
	 * This method updates the decision table and clarifies the decision table by given decision
	 *
	 * @param decisionTable
	 * @param decisionTableEntryId
	 * @param decisionCommandIndex
	 * @param sourceProperty
	 * @param targetProperty
	 * @return
	 */
	DecisionTable clarifiyDecision(DecisionTable decisionTable, int decisionTableEntryId, int decisionCommandIndex,
			String sourceProperty, String targetProperty);
}
