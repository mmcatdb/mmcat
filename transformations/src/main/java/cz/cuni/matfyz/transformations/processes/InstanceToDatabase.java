package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.Result;
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

    private AbstractPushWrapper pushWrapper;
    private Mapping mapping;
    private InstanceCategory instance;

    public void input(AbstractPushWrapper pushWrapper, Mapping mapping, InstanceCategory instance) {
        this.pushWrapper = pushWrapper;
        this.mapping = mapping;
        this.instance = instance;
    }
    
    public Result<String> run() {
        var transformation = new DMLAlgorithm();
		transformation.input(mapping, instance, "rootName", pushWrapper);
		List<DMLStatement> dmlStatements = transformation.algorithm();

        var output = new StringBuilder();
        for (DMLStatement dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent());
            output.append("\n");
        }

        return new Result<String>(output.toString());
    }

}
