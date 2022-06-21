package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.statements.DMLStatement;
import cz.cuni.matfyz.transformations.algorithms.DMLAlgorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachym.bartik
 */
public class InstanceToDatabase {

    private static Logger LOGGER = LoggerFactory.getLogger(InstanceToDatabase.class);

    private Mapping mapping;
    private InstanceCategory instance;
    private AbstractPushWrapper pushWrapper;

    public void input(Mapping mapping, InstanceCategory instance, AbstractPushWrapper pushWrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.pushWrapper = pushWrapper;
    }
    
    public DataResult<String> run() {
        var transformation = new DMLAlgorithm();
        // TODO DDL also ?
		transformation.input(mapping, instance, pushWrapper);
		List<DMLStatement> dmlStatements = transformation.algorithm();

        var output = new StringBuilder();
        for (DMLStatement dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent());
            output.append("\n");
        }

        return new DataResult<String>(output.toString());
    }

}
