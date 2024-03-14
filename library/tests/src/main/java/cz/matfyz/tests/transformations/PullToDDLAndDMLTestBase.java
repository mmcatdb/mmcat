package cz.matfyz.tests.transformations;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.matfyz.transformations.algorithms.MTCAlgorithm;

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
            forest = pullWrapper.pullForest(mapping.accessPath(), new KindNameQuery(mapping.kindName()));
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when building forest.", e);
            return;
        }

        LOGGER.trace("Pulled Forest Of Records:\n" + forest);

        var tform = new MTCAlgorithm();
        tform.input(mapping, instance, forest);
        tform.algorithm();

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
