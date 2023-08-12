package cz.cuni.matfyz.tests.transformations;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.tests.mapping.TestMapping;
import cz.cuni.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.cuni.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PullToDDLAndDMLTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullToDDLAndDMLTestBase.class);

    private final AbstractPullWrapper pullWrapper;
    private final AbstractDDLWrapper ddlWrapper;
    private final AbstractDMLWrapper dmlWrapper;
    private final Mapping mapping;

    public PullToDDLAndDMLTestBase(AbstractPullWrapper pullWrapper, AbstractDDLWrapper ddlWrapper, AbstractDMLWrapper dmlWrapper, TestMapping testMapping) {
        this.pullWrapper = pullWrapper;
        this.ddlWrapper = ddlWrapper;
        this.dmlWrapper = dmlWrapper;
        this.mapping = testMapping.mapping();
    }

    public void run() {
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(mapping.accessPath(), PullQuery.fromKindName(mapping.kindName()));
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when building forest.", e);
            return;
        }

        LOGGER.trace("Pulled Forest Of Records:\n" + forest);
        
        var transformation = new MTCAlgorithm();
        transformation.input(mapping, instance, forest);
        transformation.algorithm();

        LOGGER.trace("Created Instance Category:\n" + instance);
        
        var ddlAlgorithm = new DDLAlgorithm();
        ddlAlgorithm.input(mapping, instance, ddlWrapper);
        var ddlStatement = ddlAlgorithm.algorithm();

        LOGGER.info("Created DDL Statement:\n" + ddlStatement.getContent());

        var dmlAlgorithm = new DMLAlgorithm();
        dmlAlgorithm.input(mapping, instance, dmlWrapper);
        var dmlStatements = dmlAlgorithm.algorithm();

        LOGGER.info("Created DML Statement-s:\n" + String.join("\n", dmlStatements.stream().map(statement -> statement.getContent()).toList()));

        // TODO assert
    }
}
