package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
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
    private InstanceCategory currentInstance;
    private AbstractDDLWrapper ddlWrapper;
    private AbstractPushWrapper pushWrapper;

    public void input(Mapping mapping, InstanceCategory currentInstance, AbstractDDLWrapper ddlWrapper, AbstractPushWrapper pushWrapper) {
        this.mapping = mapping;
        this.currentInstance = currentInstance;
        this.ddlWrapper = ddlWrapper;
        this.pushWrapper = pushWrapper;
    }
    
    public DataResult<String> run() {
        
        Statistics.start(Interval.INSTANCE_TO_DATABASE);

        InstanceCategory instance = currentInstance != null ?
            currentInstance :
            new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        var ddlTransformation = new DDLAlgorithm();
        ddlTransformation.input(mapping, instance, ddlWrapper);
        
        var dmlTransformation = new DMLAlgorithm();
		dmlTransformation.input(mapping, instance, pushWrapper);

        Statistics.start(Interval.CTM_ALGORIGHM);
        DDLStatement ddlStatement = ddlTransformation.algorithm();
		List<DMLStatement> dmlStatements = dmlTransformation.algorithm();
        Statistics.end(Interval.CTM_ALGORIGHM);

        var output = new StringBuilder();
        output.append(ddlStatement.getContent())
            .append("\n");

        Statistics.set(Counter.CREATED_STATEMENTS, dmlStatements.size());

        for (DMLStatement dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent())
                .append("\n");
        }

        Statistics.end(Interval.INSTANCE_TO_DATABASE);

        return new DataResult<String>(output.toString());
    }

}
