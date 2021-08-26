package de.hda.fbi.modules.schemaextraction.impl;

import de.hda.fbi.modules.schemaextraction.AnalyseManager;
import de.hda.fbi.modules.schemaextraction.DecisionManager;
import de.hda.fbi.modules.schemaextraction.DecisionTableCreator;
import de.hda.fbi.modules.schemaextraction.ExtractionFacade;
import de.hda.fbi.modules.schemaextraction.TreeIntegrationManager;
import de.hda.fbi.modules.schemaextraction.common.ExtractionResult;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionException;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;
import de.hda.fbi.modules.schemaextraction.tree.TreeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtractionFacadeImpl implements ExtractionFacade {

//    private static Logger LOGGER = LoggerFactory.getLogger(ExtractionFacadeImpl.class);

    private DecisionManager decisionManager;

    private DecisionTableCreator decisionTableCreator;

    private AnalyseManager analyseManager;

    private TreeIntegrationManager treeIntegrationManager;

    @Autowired
    public ExtractionFacadeImpl(DecisionManager decisionManager, DecisionTableCreator decisionTableCreator,
                                AnalyseManager analyseManager, TreeIntegrationManager treeIntegrationManager) {

        this.decisionManager = decisionManager;
        this.decisionTableCreator = decisionTableCreator;
        this.analyseManager = analyseManager;
        this.treeIntegrationManager = treeIntegrationManager;
    }

    public ExtractionResult start(SchemaExtractionConfiguration extractionConfiguration)
            throws SchemaExtractionException {
//        LOGGER.debug("Starting extraction with configuration {}", extractionConfiguration);
        ExtractionResult extractionResult = new ExtractionResult();

        TreeHolder.getInstance().Clear();

        // try catch
        this.treeIntegrationManager.integrate(extractionConfiguration);

        switch (extractionConfiguration.getUseCase()) {
            case AnalyseDatabase:

                extractionResult.setAnalyseResult(this.analyseManager.analyse(extractionConfiguration));
                extractionResult.setSuccess(true);
                return extractionResult;

            case Incremental:
                break;
            case Initial:
                break;
            default:
                break;
        }

        DecisionTable decisionTable;

        decisionTable = this.decisionTableCreator.createDecisionTable(extractionConfiguration);

        return new ExtractionResult(true, decisionTable);

    }

    public ExtractionResult clarifiyDecision(DecisionTable decisionTable, int decisionTableEntryId,
                                             Integer decisionCommandIndex, String sourceProperty, String targetProperty) {

        DecisionTable table = this.decisionManager.clarifiyDecision(decisionTable, decisionTableEntryId,
                decisionCommandIndex, sourceProperty, targetProperty);

        return new ExtractionResult(true, table);
    }
}
