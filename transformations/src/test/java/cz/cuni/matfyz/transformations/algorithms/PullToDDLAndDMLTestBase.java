package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PullToDDLAndDMLTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullToDDLAndDMLTestBase.class);

    //private final String fileNamePrefix = "pullToDDLAndDML/";

    private final AbstractPullWrapper pullWrapper;
    private final AbstractDDLWrapper ddlWrapper;
    private final AbstractDMLWrapper dmlWrapper;

    public PullToDDLAndDMLTestBase(AbstractPullWrapper pullWrapper, AbstractDDLWrapper ddlWrapper, AbstractDMLWrapper dmlWrapper) {
        this.pullWrapper = pullWrapper;
        this.ddlWrapper = ddlWrapper;
        this.dmlWrapper = dmlWrapper;
    }

    public PullToDDLAndDMLTestBase setAll(String dataFileName, SchemaCategory schema, SchemaObject rootObject, String kindName, ComplexProperty path) {
        //return setDataFileName(dataFileName).setSchema(schema).setKindName(kindName).setRootObject(rootObject).setPath(path);
        return setSchema(schema).setKindName(kindName).setRootObject(rootObject).setPath(path);
    }

    /*
    private String dataFileName;

    public PullToDDLAndDMLTestBase setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
        return this;
    }
    */

    private SchemaCategory schema;

    public PullToDDLAndDMLTestBase setSchema(SchemaCategory schema) {
        this.schema = schema;
        
        LOGGER.trace("Schema Category set:\n" + schema);

        return this;
    }

    private String kindName;

    public PullToDDLAndDMLTestBase setKindName(String kindName) {
        this.kindName = kindName;

        return this;
    }

    private SchemaObject rootObject;

    public PullToDDLAndDMLTestBase setRootObject(SchemaObject rootObject) {
        this.rootObject = rootObject;

        return this;
    }

    private ComplexProperty path;

    public PullToDDLAndDMLTestBase setPath(ComplexProperty path) {
        this.path = path;

        LOGGER.trace("Access Path set:\n" + path);

        return this;
    }

    public void testAlgorithm() {
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(path, new PullWrapperOptions.Builder().buildWithKindName(kindName));
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when building forest.", e);
            return;
        }

        LOGGER.trace("Pulled Forest Of Records:\n" + forest);
        
        Mapping mapping = new Mapping(schema, rootObject, path, kindName, null);

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
    }
}
