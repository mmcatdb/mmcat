package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.statements.DDLStatement;
import cz.cuni.matfyz.statements.DMLStatement;
import cz.cuni.matfyz.transformations.algorithms.DDLAlgorithm;
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
    private AbstractDDLWrapper ddlWrapper;
    private AbstractPushWrapper pushWrapper;

    public void input(Mapping mapping, InstanceCategory instance, AbstractDDLWrapper ddlWrapper, AbstractPushWrapper pushWrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.ddlWrapper = ddlWrapper;
        this.pushWrapper = pushWrapper;
    }
    
    public DataResult<String> run() {
        var ddlTransformation = new DDLAlgorithm();
        ddlTransformation.input(mapping, instance, ddlWrapper);
        DDLStatement ddlStatement = ddlTransformation.algorithm();

        var dmlTransformation = new DMLAlgorithm();
		dmlTransformation.input(mapping, instance, pushWrapper);
		List<DMLStatement> dmlStatements = dmlTransformation.algorithm();

        var output = new StringBuilder();
        output.append(ddlStatement.getContent())
            .append("\n");

        for (DMLStatement dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent())
                .append("\n");
        }

        return new DataResult<String>(output.toString());
    }

}
