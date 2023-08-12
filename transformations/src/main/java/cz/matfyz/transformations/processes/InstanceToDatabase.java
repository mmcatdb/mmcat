package cz.matfyz.transformations.processes;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.Statistics.Counter;
import cz.matfyz.core.utils.Statistics.Interval;
import cz.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.matfyz.transformations.algorithms.ICAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jachym.bartik
 */
public class InstanceToDatabase {

    private Mapping mapping;
    private Iterable<Mapping> allMappings;
    private InstanceCategory currentInstance;
    private AbstractDDLWrapper ddlWrapper;
    private AbstractDMLWrapper dmlWrapper;
    private AbstractICWrapper icWrapper;

    public InstanceToDatabase input(
        Mapping mapping,
        Iterable<Mapping> allMappings,
        InstanceCategory currentInstance,
        AbstractDDLWrapper ddlWrapper,
        AbstractDMLWrapper dmlWrapper,
        AbstractICWrapper icWrapper
    ) {
        this.mapping = mapping;
        this.allMappings = allMappings;
        this.currentInstance = currentInstance;
        this.ddlWrapper = ddlWrapper;
        this.dmlWrapper = dmlWrapper;
        this.icWrapper = icWrapper;

        return this;
    }

    public record InstanceToDatabaseResult(
        String statementsAsString,
        Collection<AbstractStatement> statements
    ) {}
    
    public InstanceToDatabaseResult run() {
        try {
            return innerRun();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private InstanceToDatabaseResult innerRun() {
        Statistics.start(Interval.INSTANCE_TO_DATABASE);

        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        final var ddlTransformation = new DDLAlgorithm();
        ddlTransformation.input(mapping, instance, ddlWrapper);

        final var icTransformation = new ICAlgorithm();
        icTransformation.input(mapping, allMappings, icWrapper);
        
        final var dmlTransformation = new DMLAlgorithm();
        dmlTransformation.input(mapping, instance, dmlWrapper);

        Statistics.start(Interval.CTM_ALGORIGHM);
        final var ddlStatement = ddlTransformation.algorithm();
        final var icStatement = icTransformation.algorithm();
        final var dmlStatements = dmlTransformation.algorithm();
        Statistics.end(Interval.CTM_ALGORIGHM);

        Statistics.set(Counter.CREATED_STATEMENTS, dmlStatements.size());

        final var statementsAsString = statementsToString(ddlStatement, icStatement, dmlStatements);
        final var statements = new ArrayList<AbstractStatement>();
        statements.add(ddlStatement);
        statements.add(icStatement);
        statements.addAll(dmlStatements);

        Statistics.end(Interval.INSTANCE_TO_DATABASE);

        return new InstanceToDatabaseResult(statementsAsString, statements);
    }

    private String statementsToString(AbstractStatement ddlStatement, AbstractStatement icStatement, List<AbstractStatement> dmlStatements) {
        final var output = new StringBuilder();
        output.append(ddlStatement.getContent())
            .append("\n");

        output.append(icStatement.getContent())
            .append("\n");


        for (final var dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent())
                .append("\n");
        }

        return output.toString();
    }

}
