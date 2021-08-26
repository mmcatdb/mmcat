package de.hda.fbi.modules.schemaextraction.common;

import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;

import java.io.Serializable;

/**
 * Result of the whole schema extraction
 *
 * @author daniel.mueller
 */
public class ExtractionResult implements Serializable {

    private static final long serialVersionUID = -8768790950519021207L;

    private boolean success;

    private String errorMessage;

    private DecisionTable decisionTable;

    private AnalyseResult analyseResult;

    public ExtractionResult() {

    }

    public ExtractionResult(boolean success, DecisionTable decisionTable) {
        this.setSuccess(success);
        this.decisionTable = decisionTable;
    }

    public boolean isDecisionTableClarified() {
        return this.decisionTable.isClarified();
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DecisionTable getDecisionTable() {
        return this.decisionTable;
    }

    public AnalyseResult getAnalyseResult() {
        return analyseResult;
    }

    public void setAnalyseResult(AnalyseResult analyseResult) {
        this.analyseResult = analyseResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
